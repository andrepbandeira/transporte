package transporte.desafio.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import transporte.desafio.application.dto.PesagemEvent;
import transporte.desafio.application.ports.in.ProcessarPesagemUseCase;
import transporte.desafio.application.ports.out.BalancaRepositoryPort;
import transporte.desafio.application.ports.out.CaminhaoRepositoryPort;
import transporte.desafio.application.ports.out.DocaRepositoryPort;
import transporte.desafio.application.ports.out.PesagemRepositoryPort;
import transporte.desafio.application.ports.out.TipoGraoRepositoryPort;
import transporte.desafio.application.ports.out.TransacaoRepositoryPort;
import transporte.desafio.domain.exception.BalancaNaoAutorizadaException;
import transporte.desafio.domain.exception.EntidadeNaoEncontradaException;
import transporte.desafio.domain.model.Balanca;
import transporte.desafio.domain.model.Caminhao;
import transporte.desafio.domain.model.Doca;
import transporte.desafio.domain.model.LeituraPeso;
import transporte.desafio.domain.model.Pesagem;
import transporte.desafio.domain.enums.StatusPesagem;
import transporte.desafio.domain.enums.StatusTransacao;
import transporte.desafio.domain.model.TipoGrao;
import transporte.desafio.domain.model.TransacaoTransporte;
import transporte.desafio.domain.service.AlgoritmoEstabilizacao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ProcessarPesagemService implements ProcessarPesagemUseCase {

    private final BalancaRepositoryPort balancaRepository;
    private final TransacaoRepositoryPort transacaoRepository;
    private final CaminhaoRepositoryPort caminhaoRepository;
    private final TipoGraoRepositoryPort tipoGraoRepository;
    private final DocaRepositoryPort docaRepository;
    private final PesagemRepositoryPort pesagemRepository;
    private final AlgoritmoEstabilizacao algoritmo;

    private final Map<String, EstadoEstabilizacao> estados = new ConcurrentHashMap<>();

    public ProcessarPesagemService(
            BalancaRepositoryPort balancaRepository,
            TransacaoRepositoryPort transacaoRepository,
            CaminhaoRepositoryPort caminhaoRepository,
            TipoGraoRepositoryPort tipoGraoRepository,
            DocaRepositoryPort docaRepository,
            PesagemRepositoryPort pesagemRepository,
            AlgoritmoEstabilizacao algoritmo) {
        this.balancaRepository = balancaRepository;
        this.transacaoRepository = transacaoRepository;
        this.caminhaoRepository = caminhaoRepository;
        this.tipoGraoRepository = tipoGraoRepository;
        this.docaRepository = docaRepository;
        this.pesagemRepository = pesagemRepository;
        this.algoritmo = algoritmo;
    }

    @Override
    public void processar(PesagemEvent event) {
        UUID balancaId = event.balancaId();
        String placa = event.placa();
        double pesoAtual = event.pesoAtual();
        java.time.Instant instante = event.instante();

        log.debug("Processando leitura: balanca={}, placa={}, peso={}", balancaId, placa, pesoAtual);

        Balanca balanca = balancaRepository.buscarPorId(balancaId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Balanca", balancaId));

        if (!Boolean.TRUE.equals(balanca.getAtiva())) {
            throw new BalancaNaoAutorizadaException("Balanca inativa: " + balanca.getCodigo());
        }

        TransacaoTransporte transacao = transacaoRepository.buscarTransacaoAtivaPorBalanca(balancaId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Transacao em andamento para balanca " + balancaId));

        if (transacao.getStatus() == StatusTransacao.FINALIZADA) {
            log.debug("Leitura ignorada: transacao {} ja finalizada", transacao.getId());
            return;
        }

        String chave = chaveEstado(balancaId, placa);
        EstadoEstabilizacao estado = estados.computeIfAbsent(chave, k -> new EstadoEstabilizacao(algoritmo));

        LeituraPeso leitura = new LeituraPeso(instante, pesoAtual);
        boolean estabilizado = estado.adicionarLeitura(leitura, placa);

        if (estabilizado) {
            persistirPesagem(transacao, balanca, estado, placa);
            estado.reset();
        }
    }
    private void persistirPesagem(TransacaoTransporte transacao, Balanca balanca,
                                  EstadoEstabilizacao estado, String placa) {
        UUID caminhaoId = transacao.getCaminhaoId();
        UUID tipoGraoId = transacao.getTipoGraoId();

        Caminhao caminhao = caminhaoRepository.buscarPorId(caminhaoId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Caminhao", caminhaoId));
        TipoGrao tipoGrao = tipoGraoRepository.buscarPorId(tipoGraoId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("TipoGrao", tipoGraoId));

        BigDecimal pesoBrutoBd = BigDecimal.valueOf(estado.getPesoEstabilizado())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal tara = caminhao.getTara();
        BigDecimal pesoLiquido = pesoBrutoBd.subtract(tara).max(BigDecimal.ZERO);
        BigDecimal custoCarga = calcularCustoCarga(pesoLiquido, tipoGrao.getPrecoCompraPorTonelada());

        Pesagem pesagem = new Pesagem(
                UUID.randomUUID(), transacao.getId(), balanca.getId(), caminhaoId,
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
            // Pesagem estabilizada = chegada de grao na doca -> aumenta o saldo disponivel
            doca.adicionarEstoque(pesoLiquidoKg);
            docaRepository.salvar(doca);
            log.debug("Doca atualizada: tipoGrao={}, saldo={}", tipoGraoId, doca.getPesoDisponivel());
        }
    }

    private BigDecimal calcularCustoCarga(BigDecimal pesoLiquidoKg, BigDecimal precoCompraPorTonelada) {
        return pesoLiquidoKg.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP)
                .multiply(precoCompraPorTonelada).setScale(2, RoundingMode.HALF_UP);
    }

    private String chaveEstado(UUID balancaId, String placa) {
        return balancaId.toString() + ":" + (placa != null ? placa : "");
    }

    public void clearEstados() {
        estados.clear();
    }
}

