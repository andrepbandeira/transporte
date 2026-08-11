package transporte.desafio.adapter.outbound.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import transporte.desafio.adapter.outbound.persistence.entity.CaminhaoJpaEntity;

import java.util.Optional;
import java.util.UUID;

public interface CaminhaoJpaRepository extends JpaRepository<CaminhaoJpaEntity, UUID> {

    Optional<CaminhaoJpaEntity> findByPlaca(String placa);
    boolean existsByPlaca(String placa);
}
