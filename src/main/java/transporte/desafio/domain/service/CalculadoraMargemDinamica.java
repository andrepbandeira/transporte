package transporte.desafio.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Calcula a margem de lucro dinamica e o preco de venda.
 * <p>
 * Regras (de {@code .clinerules/entidades.md}):
 * <ul>
 *   <li>Margem entre {@code min} e {@code max} (5% a 20%);</li>
 *   <li>Inversamente proporcional ao estoque disponivel na doca
 *       (menos estoque = maior margem);</li>
 *   <li>Preco de venda = precoCompra * (1 + margem);</li>
 *   <li>Custo da carga = pesoLiquidoEmToneladas * precoCompra.</li>
 * </ul>
 *
 * <p>Formula da margem:
 * {@code margem = max - (stockRatio * (max - min))}
 * onde {@code stockRatio = pesoDisponivel / capacidadeMax}.
 * Quando o estoque esta vazio (ratio = 0) a margem e maxima.
 * Quando o estoque esta cheio (ratio = 1) a margem e minima.
 */
public final class CalculadoraMargemDinamica {

    private final double margemMin;
    private final double margemMax;
    private final BigDecimal capacidadeMaxToneladas;

    public CalculadoraMargemDinamica(double margemMin, double margemMax, BigDecimal capacidadeMaxToneladas) {
        if (margemMin < 0 || margemMax < 0) throw new IllegalArgumentException("margens nao podem ser negativas");
        if (margemMin > margemMax) throw new IllegalArgumentException("margemMin > margemMax");
        this.margemMin = margemMin;
        this.margemMax = margemMax;
        this.capacidadeMaxToneladas = Objects.requireNonNullElse(capacidadeMaxToneladas, BigDecimal.valueOf(1000));
    }

    /**
     * Calcula a margem dinamica baseada no estoque disponivel.
     *
     * @param pesoDisponivelKg peso disponivel na doca em kg
     * @return margem como fração (ex: 0.10 = 10%)
     */
    public double calcularMargem(BigDecimal pesoDisponivelKg) {
        if (pesoDisponivelKg == null || capacidadeMaxToneladas.compareTo(BigDecimal.ZERO) <= 0) {
            return margemMax;
        }

        BigDecimal ratio = pesoDisponivelKg.divide(capacidadeMaxToneladas, 10, RoundingMode.HALF_UP);
        ratio = ratio.max(BigDecimal.ZERO).min(BigDecimal.ONE);

        double margem = margemMax - (ratio.doubleValue() * (margemMax - margemMin));
        return Math.max(margemMin, Math.min(margemMax, margem));
    }

    /**
     * Calcula o preco de venda = precoCompra * (1 + margem).
     *
     * @param precoCompraPorTonelada preco de compra em R$/ton
     * @param pesoDisponivelKg      estoque na doca em kg
     * @return preco de venda por tonelada
     */
    public BigDecimal calcularPrecoVenda(BigDecimal precoCompraPorTonelada, BigDecimal pesoDisponivelKg) {
        double margem = calcularMargem(pesoDisponivelKg);
        return precoCompraPorTonelada
                .multiply(BigDecimal.ONE.add(BigDecimal.valueOf(margem)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula o custo da carga: (pesoLiquido / 1000) * precoCompra.
     *
     * @param pesoLiquidoKg         peso liquido em kg
     * @param precoCompraPorTonelada preco de compra em R$/ton
     * @return custo da carga em R$
     */
    public BigDecimal calcularCustoCarga(BigDecimal pesoLiquidoKg, BigDecimal precoCompraPorTonelada) {
        if (pesoLiquidoKg == null || precoCompraPorTonelada == null) return BigDecimal.ZERO;
        return pesoLiquidoKg
                .divide(BigDecimal.valueOf(1000), 10, RoundingMode.HALF_UP)
                .multiply(precoCompraPorTonelada)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public double getMargemMin() { return margemMin; }
    public double getMargemMax() { return margemMax; }
    public BigDecimal getCapacidadeMaxToneladas() { return capacidadeMaxToneladas; }
}
