package transporte.desafio.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import transporte.desafio.application.dto.EficienciaBalancaDto;
import transporte.desafio.application.dto.LucratividadeGraoDto;
import transporte.desafio.application.dto.VolumeFilialDto;
import transporte.desafio.application.ports.in.RelatorioUseCase;
import transporte.desafio.application.ports.out.BalancaRepositoryPort;
import transporte.desafio.application.ports.out.FilialRepositoryPort;
import transporte.desafio.application.ports.out.PesagemRepositoryPort;
import transporte.desafio.application.ports.out.TipoGraoRepositoryPort;
import transporte.desafio.application.ports.out.TransacaoRepositoryPort;
import transporte.desafio.domain.model.*;
import transporte.desafio.domain.service.CalculadoraMargemDinamica;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servico de relatorios administrativos (lucratividade, volume, eficiencia).
 */
@Service
@Slf4j
public class RelatorioService implements RelatorioUseCase {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final PesagemRepositoryPort pesagemRepository;
    private final TransacaoRepositoryPort transacaoRepository;
    private final TipoGraoRepositoryPort tipoGraoRepository;
    private final FilialRepositoryPort filialRepository;
    private final BalancaRepositoryPort balancaRepository;
    private final CalculadoraMargemDinamica calculadora;

    public RelatorioService(PesagemRepositoryPort pesagemRepository,
                            TransacaoRepositoryPort transacaoRepository,
                            TipoGraoRepositoryPort tipoGraoRepository,
                            FilialRepositoryPort filialRepository,
                            BalancaRepositoryPort balancaRepository,
                            CalculadoraMargemDinamica calculadora) {
        this.pesagemRepository = pesagemRepository;
        this.transacaoRepository = transacaoRepository;
        this.tipoGraoRepository = tipoGraoRepository;
        this.filialRepository = filialRepository;
        this.balancaRepository = balancaRepository;
        this.calculadora = calculadora;
    }

    @Override
    public List<LucratividadeGraoDto> lucratividadePorTipoGrao() {
        Map<UUID, String> nomes = tipoGraoRepository.listar().stream()
                .collect(Collectors.toMap(TipoGrao::getId, TipoGrao::getNome));
        Map<UUID, BigDecimal> precos = tipoGraoRepository.listar().stream()
                .collect(Collectors.toMap(TipoGrao::getId, TipoGrao::getPrecoCompraPorTonelada));

        return pesagemRepository.listar().stream()
                .collect(Collectors.groupingBy(Pesagem::getTipoGraoId))
                .entrySet().stream()
                .map(entry -> {
                    UUID tipoGraoid = entry.getKey();
                    List<Pesagem> pesagems = entry.getValue();
                    String nome = nomes.getOrDefault(tipoGraoid, "?");
                    BigDecimal precoCompra = precos.getOrDefault(tipoGraoid, ZERO);

                    BigDecimal volume = pesagems.stream()
                            .map(Pesagem::getPesoLiquido)
                            .reduce(ZERO, BigDecimal::add);
                    double margem = calculadora.calcularMargem(volume);
                    BigDecimal precoVenda = precoCompra.multiply(BigDecimal.ONE.add(BigDecimal.valueOf(margem)));
                    BigDecimal lucroPorTon = precoVenda.subtract(precoCompra);

                    return new LucratividadeGraoDto(
                            nome,
                            precoCompra.setScale(2, RoundingMode.HALF_UP),
                            precoVenda.setScale(2, RoundingMode.HALF_UP),
                            BigDecimal.valueOf(margem).setScale(4, RoundingMode.HALF_UP),
                            lucroPorTon.setScale(2, RoundingMode.HALF_UP),
                            (long) pesagems.size());
                })
                .toList();
    }

    @Override
    public List<VolumeFilialDto> volumePorFilial(LocalDate dataInicio, LocalDate dataFim) {
        Map<UUID, String> filialNomes = filialRepository.listar().stream()
                .collect(Collectors.toMap(Filial::getId, Filial::getNome));
        Map<UUID, String> graoNomes = tipoGraoRepository.listar().stream()
                .collect(Collectors.toMap(TipoGrao::getId, TipoGrao::getNome));
        Map<UUID, UUID> transacaoFilial = transacaoRepository.listar().stream()
                .collect(Collectors.toMap(TransacaoTransporte::getId, TransacaoTransporte::getFilialId));

        return pesagemRepository.listar().stream()
                .filter(p -> filtrarPorPeriodo(p.getDataHoraPesagem(), dataInicio, dataFim))
                .map(p -> {
                    UUID filialId = transacaoFilial.getOrDefault(p.getTransacaoId(), p.getTipoGraoId());
                    return new Object[]{filialId, p.getTipoGraoId(), p.getPesoLiquido()};
                })
                .collect(Collectors.groupingBy(arr -> ((UUID) arr[0]) + "|" + ((UUID) arr[1])))
                .entrySet().stream()
                .map(e -> {
                    String[] keys = e.getKey().split("\\|");
                    UUID filialId = UUID.fromString(keys[0]);
                    UUID tipoGraoid = UUID.fromString(keys[1]);
                    BigDecimal volume = e.getValue().stream()
                            .map(arr -> (BigDecimal) arr[2])
                            .reduce(ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
                    return new VolumeFilialDto(
                            filialNomes.getOrDefault(filialId, filialId.toString()),
                            graoNomes.getOrDefault(tipoGraoid, tipoGraoid.toString()),
                            volume);
                })
                .toList();
    }

    @Override
    public List<EficienciaBalancaDto> eficienciaPorBalanca() {
        Map<UUID, String> balancaNomes = balancaRepository.listar().stream()
                .collect(Collectors.toMap(Balanca::getId, Balanca::getNome));

        return pesagemRepository.listar().stream()
                .collect(Collectors.groupingBy(Pesagem::getBalancaId))
                .entrySet().stream()
                .map(entry -> {
                    UUID balancaId = entry.getKey();
                    List<Pesagem> pesagems = entry.getValue();
                    double avgMs = pesagems.stream()
                            .mapToLong(this::calcularTempoEstabilizacaoMs)
                            .average().orElse(0);
                    return new EficienciaBalancaDto(
                            balancaNomes.getOrDefault(balancaId, balancaId.toString()),
                            (long) avgMs,
                            (long) pesagems.size());
                })
                .toList();
    }

    private boolean filtrarPorPeriodo(LocalDateTime dataHora, LocalDate inicio, LocalDate fim) {
        if (dataHora == null) return true;
        LocalDate data = dataHora.toLocalDate();
        boolean okInicio = inicio == null || !data.isBefore(inicio);
        boolean okFim = fim == null || !data.isAfter(fim);
        return okInicio && okFim;
    }

    private long calcularTempoEstabilizacaoMs(Pesagem pesagem) {
        if (pesagem.getCreatedAt() != null && pesagem.getDataHoraPesagem() != null) {
            long ms = Duration.between(pesagem.getCreatedAt(), pesagem.getDataHoraPesagem()).toMillis();
            return Math.max(0, ms);
        }
        return 0L;
    }
}
