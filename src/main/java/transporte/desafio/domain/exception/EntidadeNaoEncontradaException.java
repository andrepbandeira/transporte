package transporte.desafio.domain.exception;

/**
 * Lancada quando uma entidade referenciada nao existe no banco.
 */
public class EntidadeNaoEncontradaException extends RegraDeNegocioException {

    public EntidadeNaoEncontradaException(String message) {
        super(message);
    }

    public EntidadeNaoEncontradaException(String entityName, Object id) {
        super(String.format("%s nao encontrada com id: %s", entityName, id));
    }
}
