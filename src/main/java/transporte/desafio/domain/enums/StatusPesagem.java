package transporte.desafio.domain.enums;

/**
 * Estados da pesagem dentro do algoritmo de estabilizacao.
 * <p>
 * PENDENTE      — aguardando mais leituras para decidir.
 * ESTABILIZADA   — peso consolidado e pronto para persistencia.
 * INVALIDA       — leitura descartada (ex: balanca zerada prematuramente).
 */
public enum StatusPesagem {
    PENDENTE,
    ESTABILIZADA,
    INVALIDA
}
