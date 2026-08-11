package transporte.desafio.domain.exception;

/**
 * Lancada quando um consumidor tenta estabilizar uma transacao ja concluida.
 * Garante idempotencia: apos a pesagem estabilizada, novas leituras sao ignoradas.
 */
public class TransacaoJaEstabilizadaException extends RegraDeNegocioException {

    public TransacaoJaEstabilizadaException(String message) {
        super(message);
    }
}
