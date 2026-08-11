package transporte.desafio.domain.exception;

/**
 * Excecao base para regras de negocio violadas na camada de dominio/use-cases.
 */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String message) {
        super(message);
    }

    public RegraDeNegocioException(String message, Throwable cause) {
        super(message, cause);
    }
}
