package transporte.desafio.adapter.outbound.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import transporte.desafio.adapter.outbound.persistence.entity.PesagemJpaEntity;

import java.util.List;
import java.util.UUID;

public interface PesagemJpaRepository extends JpaRepository<PesagemJpaEntity, UUID> {

    List<PesagemJpaEntity> findByTransacaoId(UUID transacaoId);
    List<PesagemJpaEntity> findByTipoGraoId(UUID tipoGraoId);
    List<PesagemJpaEntity> findByBalancaId(UUID balancaId);
}
