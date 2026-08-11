package transporte.desafio.adapter.outbound.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade JPA de caminhao.
 */
@Entity
@Table(name = "caminhao")
public class CaminhaoJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "placa", unique = true, nullable = false, length = 20)
    private String placa;

    @Column(name = "tara", nullable = false, precision = 10, scale = 2)
    private BigDecimal tara = BigDecimal.ZERO;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected CaminhaoJpaEntity() {
    }

    public CaminhaoJpaEntity(UUID id, String placa, BigDecimal tara, String descricao,
                             Boolean ativo, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.placa = placa;
        this.tara = tara;
        this.descricao = descricao;
        this.ativo = ativo;
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
    public String getPlaca() { return placa; }
    public BigDecimal getTara() { return tara; }
    public String getDescricao() { return descricao; }
    public Boolean getAtivo() { return ativo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
