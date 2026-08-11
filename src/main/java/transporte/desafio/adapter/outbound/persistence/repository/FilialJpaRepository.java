package transporte.desafio.adapter.outbound.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import transporte.desafio.adapter.outbound.persistence.entity.FilialJpaEntity;

import java.util.UUID;

public interface FilialJpaRepository extends JpaRepository<FilialJpaEntity, UUID> {
}
