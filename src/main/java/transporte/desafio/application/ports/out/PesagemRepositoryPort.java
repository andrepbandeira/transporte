package transporte.desafio.application.ports.out;

import transporte.desafio.domain.model.Pesagem;

import java.util.List;
import java.util.UUID;

/**
 * Porta de saida: repositorio de pesagens consolidadas.
 */
public interface PesagemRepositoryPort {

    Pesagem salvar(Pesagem pesagem);
    List<Pesagem> listar();
    List<Pesagem> listarPorTransacao(UUID transacaoId);
    List<Pesagem> listarPorTipoGrao(UUID tipoGraoId);
}
