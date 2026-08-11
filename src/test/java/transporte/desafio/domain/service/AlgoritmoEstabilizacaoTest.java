package transporte.desafio.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import transporte.desafio.domain.model.LeituraPeso;
import transporte.desafio.domain.model.ResultadoEstabilizacao;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitarios do algoritmo de estabilizacao de peso.
 */
class AlgoritmoEstabilizacaoTest {

    private static final int JANELA = 5;
    private static final double DELTA_MAX = 2.0;
    private static final long INTERVALO_MIN_MS = 200;

    private Instant base;
    private AlgoritmoEstabilizacao algoritmo;

    @BeforeEach
    void setUp() {
        base = Instant.parse("2026-08-11T10:00:00Z");
        algoritmo = new AlgoritmoEstabilizacao(JANELA, DELTA_MAX, INTERVALO_MIN_MS);
    }

    private List<LeituraPeso> construirLeituras(double... pesos) {
        List<LeituraPeso> lista = new ArrayList<>();
        for (int i = 0; i < pesos.length; i++) {
            lista.add(new LeituraPeso(base.plusMillis((long) (i + 1) * 100), pesos[i]));
        }
        return lista;
    }

    @Test
    @DisplayName("estabiliza com leituras dentro do delta e do intervalo minimo")
    void testEstabilizaComLeiturasEstaveis() {
        ResultadoEstabilizacao resultado = algoritmo.verificar(
                construirLeituras(1000.0, 1000.1, 1000.0, 1000.2, 1000.1));
        assertThat(resultado).isEqualTo(ResultadoEstabilizacao.ESTABILIZADO);
    }

    @Test
    @DisplayName("nao estabiliza quando a variacao excede o delta maximo")
    void testNaoEstabilizaComLeiturasFlutuantes() {
        ResultadoEstabilizacao resultado = algoritmo.verificar(
                construirLeituras(1000.0, 1005.0, 1000.0, 1010.0, 1000.0));
        assertThat(resultado).isEqualTo(ResultadoEstabilizacao.NAO_ESTABILIZADO);
    }

    @Test
    @DisplayName("nao estabiliza com janela insuficiente de leituras")
    void testNaoEstabilizaComJanelaInsuficiente() {
        ResultadoEstabilizacao resultado = algoritmo.verificar(
                construirLeituras(1000.0, 1000.1, 1000.0));
        assertThat(resultado).isEqualTo(ResultadoEstabilizacao.INSUFICIENTE_DADOS);
    }

    @Test
    @DisplayName("nao estabiliza com lista vazia")
    void testNaoEstabilizaComListaVazia() {
        ResultadoEstabilizacao resultado = algoritmo.verificar(Collections.emptyList());
        assertThat(resultado).isEqualTo(ResultadoEstabilizacao.NAO_ESTABILIZADO);
    }

    @Test
    @DisplayName("nao estabiliza com leitura unica")
    void testNaoEstabilizaComLeituraUnica() {
        ResultadoEstabilizacao resultado = algoritmo.verificar(construirLeituras(1000.0));
        assertThat(resultado).isEqualTo(ResultadoEstabilizacao.INSUFICIENTE_DADOS);
    }

    @Test
    @DisplayName("estabiliza quando a variacao e igual ao limite exato")
    void testEstabilizaComDeltaNoLimite() {
        // delta = 1002.0 - 1000.0 = 2.0 = DELTA_MAX
        ResultadoEstabilizacao resultado = algoritmo.verificar(
                construirLeituras(1000.0, 1001.0, 1000.0, 1002.0, 1001.0));
        assertThat(resultado).isEqualTo(ResultadoEstabilizacao.ESTABILIZADO);
    }
}
