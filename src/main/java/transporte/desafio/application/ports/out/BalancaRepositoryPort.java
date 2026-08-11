package transporte.desafio.application.ports.out;

import transporte.desafio.domain.model.Balanca;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de saida: repositorio de balancas.
 */
public interface BalancaRepositoryPort {

    Balanca salvar(Balanca balanca);
    Optional<Balanca> buscarPorId(UUID id);
    Optional<Balanca> buscarPorCodigo(String codigo);
    List<Balanca> listar();
    List<Balanca> listarPorFilial(UUID filialId);
    boolean existsById(UUID id);
    void deleteById(UUID id);
}
