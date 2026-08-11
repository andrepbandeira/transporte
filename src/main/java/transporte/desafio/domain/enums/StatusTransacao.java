package transporte.desafio.domain.enums;

/**
 * Estados do ciclo de vida de uma transacao de transporte.
 * <p>
 * EM_ANDAMENTO — o caminhao esta em trânsito ou na balanca.
 * FINALIZADA  — a pesagem foi estabilizada e o ciclo encerrado.
 * CANCELADA   — a operacao foi cancelada antes de ser concluida.
 */
public enum StatusTransacao {
    EM_ANDAMENTO,
    FINALIZADA,
    CANCELADA
}
