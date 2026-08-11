package transporte.desafio.application.ports.out;

import transporte.desafio.domain.model.TransacaoTransporte;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de saida: repositorio de transacoes de transporte.
 */
public interface TransacaoRepositoryPort {

    TransacaoTransporte salvar(TransacaoTransporte transacao);
    Optional<TransacaoTransporte> buscarPorId(UUID id);
    
    /**
     * Busca a transacao ativa (nao finalizada) para uma balanca e placa.
     * Usado para idempotencia no consumidor Kafka.
     */
    Optional<TransacaoTransporte> buscarTransacaoAtiva(UUID balancaId, String placa);
    
    /**
     * Busca a transacao ativa (nao finalizada) para uma balanca.
     * Usado quando a placa e vazia (balanca voltou a zero).
     */
    Optional<TransacaoTransporte> buscarTransacaoAtivaPorBalanca(UUID balancaId);
    
    List<TransacaoTransporte> listar();
    boolean existsById(UUID id);
}
