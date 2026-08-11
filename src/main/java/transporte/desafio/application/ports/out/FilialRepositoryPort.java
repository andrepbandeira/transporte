package transporte.desafio.application.ports.out;

import transporte.desafio.domain.model.Filial;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de saida: repositorio de filiais.
 */
public interface FilialRepositoryPort {

    Filial salvar(Filial filial);
    Optional<Filial> buscarPorId(UUID id);
    List<Filial> listar();
    boolean existsById(UUID id);
}
