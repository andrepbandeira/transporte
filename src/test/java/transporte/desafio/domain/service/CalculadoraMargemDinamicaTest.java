package transporte.desafio.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes unitarios do calculo de margem dinamica.
 */
class CalculadoraMargemDinamicaTest {

    private CalculadoraMargemDinamica calculadora;

    @BeforeEach
    void setUp() {
        calculadora = new CalculadoraMargemDinamica(0.05, 0.20, BigDecimal.valueOf(1000));
    }

    @Test
    @DisplayName("estoque vazio rende a margem maxima")
    void testMargemMaximaComEstoqueZero() {
        double margem = calculadora.calcularMargem(BigDecimal.ZERO);
        assertThat(margem).isEqualTo(0.20);
    }

    @Test
    @DisplayName("estoque cheio (capacidade) rende a margem minima")
    void testMargemMinimaComEstoqueCheio() {
        double margem = calculadora.calcularMargem(BigDecimal.valueOf(1000));
        assertThat(margem).isEqualTo(0.05);
    }

    @Test
    @DisplayName("estoque na metade rende margem intermediaria")
    void testMargemProporcional() {
        double margem = calculadora.calcularMargem(BigDecimal.valueOf(500));
        // ratio = 500/1000 = 0.5 -> margem = 0.20 - (0.5 * 0.15) = 0.125
        assertThat(margem).isEqualTo(0.125);
    }

    @Test
    @DisplayName("preco de venda aplica a margem calculada")
    void testPrecoVendaComMargem() {
        BigDecimal precoVenda = calculadora.calcularPrecoVenda(
                BigDecimal.valueOf(100), BigDecimal.valueOf(500));
        // margem = 0.125 -> precoVenda = 100 * 1.125 = 112.50
        assertThat(precoVenda).isEqualByComparingTo(new BigDecimal("112.50"));
    }

    @Test
    @DisplayName("custo da carga usa peso liquido e preco de compra")
    void testCustoCarga() {
        BigDecimal custo = calculadora.calcularCustoCarga(
                BigDecimal.valueOf(10000), BigDecimal.valueOf(180));
        // 10 toneladas * 180 = 1800.00
        assertThat(custo).isEqualByComparingTo(new BigDecimal("1800.00"));
    }

    @Test
    @DisplayName("rejeita margens invalidas")
    void testMargensInvalidas() {
        assertThatThrownBy(() -> new CalculadoraMargemDinamica(-0.1, 0.20, BigDecimal.valueOf(1000)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CalculadoraMargemDinamica(0.30, 0.20, BigDecimal.valueOf(1000)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
