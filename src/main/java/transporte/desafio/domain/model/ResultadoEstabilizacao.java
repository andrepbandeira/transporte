package transporte.desafio.domain.model;

/**
 * Resultado da verificacao de estabilizacao.
 */
public enum ResultadoEstabilizacao {
    /** Pesagem estabilizada — pronta para persistencia. */
    ESTABILIZADO,
    /** Ainda nao atingiu o criterio de estabilizacao. */
    NAO_ESTABILIZADO,
    /** Dados insuficientes na janela atual. */
    INSUFICIENTE_DADOS
}
