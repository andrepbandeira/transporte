package transporte.desafio.adapter.outbound.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import transporte.desafio.adapter.outbound.persistence.entity.TransacaoTransporteJpaEntity;
import transporte.desafio.domain.enums.StatusTransacao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransacaoTransporteJpaRepository extends JpaRepository<TransacaoTransporteJpaEntity, UUID> {

    /**
     * Busca a transacao ativa (nao finalizada/cancelada) para uma balanca e placa.
     */
    @Query("""
            SELECT t FROM TransacaoTransporteJpaEntity t
            WHERE t.balancaId = :balancaId
              AND t.caminhaoId IN (SELECT c.id FROM CaminhaoJpaEntity c WHERE c.placa = :placa)
              AND t.status IN :ativos
            ORDER BY t.dataInicio DESC
            """)
    Optional<TransacaoTransporteJpaEntity> findTransacaoAtivaPorBalancaEPlaca(
            @Param("balancaId") UUID balancaId,
            @Param("placa") String placa,
            @Param("ativos") List<StatusTransacao> ativos);

    /**
     * Busca a transacao ativa (nao finalizada/cancelada) para uma balanca.
     */
    @Query("""
            SELECT t FROM TransacaoTransporteJpaEntity t
            WHERE t.balancaId = :balancaId
              AND t.status IN :ativos
            ORDER BY t.dataInicio DESC
            """)
    List<TransacaoTransporteJpaEntity> findTransacaoAtivaPorBalanca(
            @Param("balancaId") UUID balancaId,
            @Param("ativos") List<StatusTransacao> ativos);
}
