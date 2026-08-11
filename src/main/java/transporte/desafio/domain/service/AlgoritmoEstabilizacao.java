package transporte.desafio.domain.service;

import transporte.desafio.domain.model.LeituraPeso;
import transporte.desafio.domain.model.ResultadoEstabilizacao;

import java.time.Duration;
import java.util.DoubleSummaryStatistics;
import java.util.List;

/**
 * Algoritmo puro de estabilizacao de peso.
 * <p>
 * Regra pratica (de {@code .clinerules/entidades.md}):
 * <pre>
 *   Se a diferenca entre o maior e o menor peso da janela for menor que
 *   {@code maxDeltaKg} por {@code minIntervalMs} consecutivos, considere
 *   a balanca estabilizada.
 * </pre>
 *
 * <p>O algoritmo verifica três condicoes sobre a janela de leituras
 * fornecida:
 * <ol>
 *   <li>A janela deve ter pelo menos {@code windowReadings} leituras</li>
 *   <li>A variacao (max - min) deve ser <= {@code maxDeltaKg}</li>
 *   <li>O tempo entre a primeira e ultima leitura da janela deve ser >= {@code minIntervalMs}</li>
 * </ol>
 *
 * @param windowReadings tamanho minimo da janela
 * @param maxDeltaKg     variacao tolerada em kg
 * @param minIntervalMs  duracao minima em ms para considerar estabilizado
 */
public final class AlgoritmoEstabilizacao {

    private final int windowReadings;
    private final double maxDeltaKg;
    private final long minIntervalMs;

    public AlgoritmoEstabilizacao(int windowReadings, double maxDeltaKg, long minIntervalMs) {
        if (windowReadings < 1) throw new IllegalArgumentException("windowReadings must be >= 1");
        if (maxDeltaKg < 0) throw new IllegalArgumentException("maxDeltaKg must be >= 0");
        if (minIntervalMs < 0) throw new IllegalArgumentException("minIntervalMs must be >= 0");
        this.windowReadings = windowReadings;
        this.maxDeltaKg = maxDeltaKg;
        this.minIntervalMs = minIntervalMs;
    }

    /**
     * Verifica se as leituras fornecidas indicam estabilizacao.
     *
     * @param leituras lista cronologica de leituras (do mais antigo ao mais recente)
     * @return {@link ResultadoEstabilizacao}
     */
    public ResultadoEstabilizacao verificar(List<LeituraPeso> leituras) {
        if (leituras == null || leituras.isEmpty()) {
            return ResultadoEstabilizacao.NAO_ESTABILIZADO;
        }

        // Usa a ultima janela (windowReadings)
        List<LeituraPeso> window;
        if (leituras.size() > windowReadings) {
            window = leituras.subList(leituras.size() - windowReadings, leituras.size());
        } else {
            window = leituras;
        }

        if (window.size() < windowReadings) {
            return ResultadoEstabilizacao.INSUFICIENTE_DADOS;
        }

        DoubleSummaryStatistics stats = window.stream()
                .mapToDouble(LeituraPeso::pesoKg)
                .summaryStatistics();

        double delta = stats.getMax() - stats.getMin();
        if (delta > maxDeltaKg) {
            return ResultadoEstabilizacao.NAO_ESTABILIZADO;
        }

        // Verifica duracao da janela
        if (window.size() >= 2 && minIntervalMs > 0) {
            long durationMs = Duration.between(
                    window.get(0).instante(),
                    window.get(window.size() - 1).instante()).toMillis();
            if (durationMs < minIntervalMs) {
                return ResultadoEstabilizacao.NAO_ESTABILIZADO;
            }
        }

        return ResultadoEstabilizacao.ESTABILIZADO;
    }

    public int getWindowReadings() { return windowReadings; }
    public double getMaxDeltaKg() { return maxDeltaKg; }
    public long getMinIntervalMs() { return minIntervalMs; }
}
