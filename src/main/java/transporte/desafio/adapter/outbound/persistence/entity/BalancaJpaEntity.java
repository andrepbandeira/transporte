package transporte.desafio.adapter.outbound.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade JPA de balanca.
 */
@Entity
@Table(name = "balanca")
public class BalancaJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo", unique = true, nullable = false)
    private String codigo;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "filial_id", nullable = false)
    private UUID filialId;

    @Column(name = "ativa", nullable = false)
    private Boolean ativa = true;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected BalancaJpaEntity() {
    }

    public BalancaJpaEntity(UUID id, String codigo, String password, String nome,
                            UUID filialId, Boolean ativa, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.codigo = codigo;
        this.password = password;
        this.nome = nome;
        this.filialId = filialId;
        this.ativa = ativa;
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
    public String getCodigo() { return codigo; }
    public String getPassword() { return password; }
    public String getNome() { return nome; }
    public UUID getFilialId() { return filialId; }
    public Boolean getAtiva() { return ativa; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
