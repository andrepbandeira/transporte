package transporte.desafio.application.ports.out;

import transporte.desafio.domain.model.Caminhao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de saida: repositorio de caminhoes.
 */
public interface CaminhaoRepositoryPort {

    Caminhao salvar(Caminhao caminhao);
    Optional<Caminhao> buscarPorId(UUID id);
    Optional<Caminhao> buscarPorPlaca(String placa);
    List<Caminhao> listar();
    boolean existsById(UUID id);
}
