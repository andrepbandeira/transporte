package transporte.desafio.application.ports.out;

import transporte.desafio.domain.model.Doca;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de saida: repositorio de docas (estoque por tipo de grao).
 */
public interface DocaRepositoryPort {

    Doca salvar(Doca doca);
    Optional<Doca> buscarPorId(UUID id);
    Optional<Doca> buscarPorTipoGrao(UUID tipoGraoId);
    List<Doca> listar();
    boolean existsByTipoGrao(UUID tipoGraoId);
}
