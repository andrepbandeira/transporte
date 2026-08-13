package transporte.desafio.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import transporte.desafio.application.dto.PesagemEvent;
import transporte.desafio.application.dto.ScaleStabilizedEvent;
import transporte.desafio.application.dto.WeightReadingDto;
import transporte.desafio.application.ports.in.ProcessarPesagemUseCase;
import transporte.desafio.application.ports.out.BalancaRepositoryPort;
import transporte.desafio.application.ports.out.CaminhaoRepositoryPort;
import transporte.desafio.application.ports.out.DocaRepositoryPort;
import transporte.desafio.application.ports.out.PesagemRepositoryPort;
import transporte.desafio.application.ports.out.TipoGraoRepositoryPort;
import transporte.desafio.application.ports.out.TransacaoRepositoryPort;
import transporte.desafio.domain.enums.StatusPesagem;
import transporte.desafio.domain.enums.StatusTransacao;
import transporte.desafio.domain.exception.EntidadeNaoEncontradaException;
import transporte.desafio.domain.model.Balanca;
import transporte.desafio.domain.model.Caminhao;
import transporte.desafio.domain.model.Doca;
import transporte.desafio.domain.model.Pesagem;
import transporte.desafio.domain.model.TipoGrao;
import transporte.desafio.domain.model.TransacaoTransporte;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class ProcessarPesagemService implements ProcessarPesagemUseCase {

    private final BalancaRepositoryPort balancaRepository;
    private final TransacaoRepositoryPort transacaoRepository;
    private final CaminhaoRepositoryPort caminhaoRepository;
    private final TipoGraoRepositoryPort tipoGraoRepository;
    private final DocaRepositoryPort docaRepository;
    private final PesagemRepositoryPort pesagemRepository;
    private final WeightStabilizationService weightStabilizationService;
    private final ScaleIngestionService scaleIngestionService;

    public ProcessarPesagemService(
            BalancaRepositoryPort balancaRepository,
            TransacaoRepositoryPort transacaoRepository,
            CaminhaoRepositoryPort caminhaoRepository,
            TipoGraoRepositoryPort tipoGraoRepository,
            DocaRepositoryPort docaRepository,
            PesagemRepositoryPort pesagemRepository,
            WeightStabilizationService weightStabilizationService,
            ScaleIngestionService scaleIngestionService) {
        this.balancaRepository = balancaRepository;
        this.transacaoRepository = transacaoRepository;
        this.caminhaoRepository = caminhaoRepository;
        this.tipoGraoRepository = tipoGraoRepository;
        this.docaRepository = docaRepository;
        this.pesagemRepository = pesagemRepository;
        this.weightStabilizationService = weightStabilizationService;
        this.scaleIngestionService = scaleIngestionService;
    }

    @Override
    public void processar(PesagemEvent event) {
        UUID balancaId = event.balancaId();
        String placa = event.placa();
        double pesoAtual = event.pesoAtual() != null ? event.pesoAtual() : 0.0;

        log.debug("Processando leitura: balanca={}, placa={}, peso={}", balancaId, placa, pesoAtual);

        Balanca balanca = balancaRepository.buscarPorId(balancaId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Balanca", balancaId));

        if (!Boolean.TRUE.equals(balanca.getAtiva())) {
            log.warn("Leitura ignorada: balanca {} esta inativa", balancaId);
            return;
        }

        TransacaoTransporte transacao = transacaoRepository.buscarTransacaoAtivaPorBalanca(balancaId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Transacao em andamento para balanca " + balancaId));

        if (transacao.getStatus() == StatusTransacao.FINALIZADA) {
            log.debug("Leitura ignorada: transacao {} ja finalizada", transacao.getId());
            return;
        }

        // Tara do caminhao vinculado a transacao (necessaria para persistencia).
        // O gate de tara (abaixo) agora usa lookup por placa via cache "truck-tares"
        // dentro do proprio ScaleIngestionService -> TruckTareService.
        Caminhao caminhao = caminhaoRepository.buscarPorId(transacao.getCaminhaoId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Caminhao", transacao.getCaminhaoId()));

        WeightReadingDto reading = new WeightReadingDto(
                balancaId.toString(),
                placa,
                BigDecimal.valueOf(pesoAtual)
        );

        // Gate de tara (ScaleIngestionService): descarta leituras abaixo de tara * fator.
        // A tara e resolvida internamente via cache "truck-tares" (TruckTareService).
        if (!scaleIngestionService.isReadingValid(reading)) {
            log.debug("Leitura descartada pelo gate de tara: placa={}, peso={}", placa, pesoAtual);
            return;
        }

        Optional<ScaleStabilizedEvent> stabilizedOpt = weightStabilizationService.processReading(reading);

        if (stabilizedOpt.isPresent()) {
            ScaleStabilizedEvent stabilizedEvent = stabilizedOpt.get();
            persistirPesagem(transacao, balanca, caminhao, stabilizedEvent.averageWeight(), placa);
        }
    }

    private void persistirPesagem(TransacaoTransporte transacao, Balanca balanca,
                                  Caminhao caminhao, BigDecimal pesoBrutoBd, String placa) {
        UUID tipoGraoId = transacao.getTipoGraoId();

        TipoGrao tipoGrao = tipoGraoRepository.buscarPorId(tipoGraoId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("TipoGrao", tipoGraoId));

        BigDecimal tara = caminhao.getTara();
        BigDecimal pesoLiquido = pesoBrutoBd.subtract(tara).max(BigDecimal.ZERO);
        BigDecimal custoCarga = calcularCustoCarga(pesoLiquido, tipoGrao.getPrecoCompraPorTonelada());

        Pesagem pesagem = new Pesagem(
                UUID.randomUUID(), transacao.getId(), balanca.getId(), caminhao.getId(),
                tipoGraoId, placa != null ? placa : "", pesoBrutoBd, pesoLiquido,
                LocalDateTime.now(ZoneId.of("America/Sao_Paulo")), custoCarga,
                StatusPesagem.ESTABILIZADA, "Pesagem estabilizada automaticamente",
                LocalDateTime.now());

        pesagemRepository.salvar(pesagem);
        atualizarDoca(tipoGraoId, pesoLiquido);

        transacao.finalizar("Pesagem estabilizada: " + pesoBrutoBd + " kg");
        transacao.adicionarPesagem(pesagem);
        transacaoRepository.salvar(transacao);

        log.info("Pesagem persistida: transacao={}, balanca={}, placa={}, bruto={}, liquido={}",
                transacao.getId(), balanca.getCodigo(), placa, pesoBrutoBd, pesoLiquido);
    }

    private void atualizarDoca(UUID tipoGraoId, BigDecimal pesoLiquidoKg) {
        Optional<Doca> optDoca = docaRepository.buscarPorTipoGrao(tipoGraoId);
        if (optDoca.isPresent()) {
            Doca doca = optDoca.get();
            doca.adicionarEstoque(pesoLiquidoKg);
            docaRepository.salvar(doca);
            log.debug("Doca atualizada: tipoGrao={}, saldo={}", tipoGraoId, doca.getPesoDisponivel());
        }
    }

    private BigDecimal calcularCustoCarga(BigDecimal pesoLiquidoKg, BigDecimal precoCompraPorTonelada) {
        return pesoLiquidoKg.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP)
                .multiply(precoCompraPorTonelada).setScale(2, RoundingMode.HALF_UP);
    }

    public void clearEstados() {
        weightStabilizationService.clearBuffers();
    }
}

