package transporte.desafio.adapter.outbound.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import transporte.desafio.adapter.outbound.persistence.entity.BalancaJpaEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BalancaJpaRepository extends JpaRepository<BalancaJpaEntity, UUID> {

    Optional<BalancaJpaEntity> findByCodigo(String codigo);
    List<BalancaJpaEntity> findByFilialId(UUID filialId);
    boolean existsByCodigo(String codigo);
}
