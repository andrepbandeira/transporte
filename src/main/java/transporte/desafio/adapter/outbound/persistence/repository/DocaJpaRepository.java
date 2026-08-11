package transporte.desafio.adapter.outbound.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import transporte.desafio.adapter.outbound.persistence.entity.DocaJpaEntity;

import java.util.Optional;
import java.util.UUID;

public interface DocaJpaRepository extends JpaRepository<DocaJpaEntity, UUID> {

    Optional<DocaJpaEntity> findByTipoGraoId(UUID tipoGraoId);
    boolean existsByTipoGraoId(UUID tipoGraoId);
}
