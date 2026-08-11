package transporte.desafio.adapter.outbound.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade JPA de doca — estoque disponivel por tipo de grao.
 */
@Entity
@Table(name = "doca")
public class DocaJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tipo_grao_id", nullable = false)
    private UUID tipoGraoId;

    @Column(name = "peso_disponivel", nullable = false, precision = 12, scale = 2)
    private BigDecimal pesoDisponivel = BigDecimal.ZERO;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected DocaJpaEntity() {
    }

    public DocaJpaEntity(UUID id, UUID tipoGraoId, BigDecimal pesoDisponivel,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.tipoGraoId = tipoGraoId;
        this.pesoDisponivel = pesoDisponivel;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getTipoGraoId() { return tipoGraoId; }
    public BigDecimal getPesoDisponivel() { return pesoDisponivel; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setPesoDisponivel(BigDecimal pesoDisponivel) { this.pesoDisponivel = pesoDisponivel; }
}
