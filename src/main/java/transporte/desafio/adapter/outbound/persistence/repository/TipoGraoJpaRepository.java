package transporte.desafio.adapter.outbound.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import transporte.desafio.adapter.outbound.persistence.entity.TipoGraoJpaEntity;

import java.util.Optional;
import java.util.UUID;

public interface TipoGraoJpaRepository extends JpaRepository<TipoGraoJpaEntity, UUID> {

    Optional<TipoGraoJpaEntity> findByNome(String nome);
    boolean existsByNome(String nome);
}
