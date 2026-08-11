package transporte.desafio.adapter.outbound.persistence.entity;

import jakarta.persistence.*;
import transporte.desafio.domain.enums.StatusTransacao;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade JPA de transacao de transporte.
 */
@Entity
@Table(name = "transacao_transporte")
public class TransacaoTransporteJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "caminhao_id", nullable = false)
    private UUID caminhaoId;

    @Column(name = "filial_id", nullable = false)
    private UUID filialId;

    @Column(name = "tipo_grao_id", nullable = false)
    private UUID tipoGraoId;

    @Column(name = "balanca_id")
    private UUID balancaId;

    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;

    @Column(name = "data_fim")
    private LocalDateTime dataFim;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private StatusTransacao status;

    @Column(name = "observacao")
    private String observacao;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected TransacaoTransporteJpaEntity() {
    }

    public TransacaoTransporteJpaEntity(UUID id, UUID caminhaoId, UUID filialId, UUID tipoGraoId,
                                        UUID balancaId, LocalDateTime dataInicio, LocalDateTime dataFim,
                                        StatusTransacao status, String observacao,
                                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.caminhaoId = caminhaoId;
        this.filialId = filialId;
        this.tipoGraoId = tipoGraoId;
        this.balancaId = balancaId;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.status = status;
        this.observacao = observacao;
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
    public UUID getCaminhaoId() { return caminhaoId; }
    public UUID getFilialId() { return filialId; }
    public UUID getTipoGraoId() { return tipoGraoId; }
    public UUID getBalancaId() { return balancaId; }
    public LocalDateTime getDataInicio() { return dataInicio; }
    public LocalDateTime getDataFim() { return dataFim; }
    public StatusTransacao getStatus() { return status; }
    public String getObservacao() { return observacao; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
