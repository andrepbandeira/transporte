package transporte.desafio.domain.exception;

/**
 * Lancada quando uma balanca tenta enviar dados sem autenticacao valida.
 */
public class BalancaNaoAutorizadaException extends RegraDeNegocioException {

    public BalancaNaoAutorizadaException(String message) {
        super(message);
    }

    public BalancaNaoAutorizadaException(String message, Throwable cause) {
        super(message, cause);
    }
}
