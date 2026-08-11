package transporte.desafio.application.ports.out;

import transporte.desafio.domain.model.TipoGrao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de saida: repositorio de tipos de grao.
 */
public interface TipoGraoRepositoryPort {

    TipoGrao salvar(TipoGrao tipoGrao);
    Optional<TipoGrao> buscarPorId(UUID id);
    Optional<TipoGrao> buscarPorNome(String nome);
    List<TipoGrao> listar();
    boolean existsById(UUID id);
}
