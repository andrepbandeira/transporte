package transporte.desafio.adapter.outbound.persistence.entity;

import jakarta.persistence.*;
import transporte.desafio.domain.enums.StatusPesagem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade JPA de pesagem consolidada (estabilizada).
 */
@Entity
@Table(name = "pesagem")
public class PesagemJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "transacao_id", nullable = false)
    private UUID transacaoId;

    @Column(name = "balanca_id", nullable = false)
    private UUID balancaId;

    @Column(name = "caminhao_id")
    private UUID caminhaoId;

    @Column(name = "tipo_grao_id", nullable = false)
    private UUID tipoGraoId;

    @Column(name = "placa", nullable = false, length = 20)
    private String placa;

    @Column(name = "peso_bruto_estabilizado", nullable = false, precision = 12, scale = 2)
    private BigDecimal pesoBrutoEstabilizado;

    @Column(name = "peso_liquido", nullable = false, precision = 12, scale = 2)
    private BigDecimal pesoLiquido;

    @Column(name = "data_hora_pesagem", nullable = false)
    private LocalDateTime dataHoraPesagem;

    @Column(name = "custo_carga", precision = 12, scale = 2)
    private BigDecimal custoCarga;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_estabilidade", nullable = false, length = 50)
    private StatusPesagem statusEstabilidade;

    @Column(name = "observacao")
    private String observacao;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected PesagemJpaEntity() {
    }

    public PesagemJpaEntity(UUID id, UUID transacaoId, UUID balancaId, UUID caminhaoId,
                            UUID tipoGraoId, String placa, BigDecimal pesoBrutoEstabilizado,
                            BigDecimal pesoLiquido, LocalDateTime dataHoraPesagem,
                            BigDecimal custoCarga, StatusPesagem statusEstabilidade,
                            String observacao, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.transacaoId = transacaoId;
        this.balancaId = balancaId;
        this.caminhaoId = caminhaoId;
        this.tipoGraoId = tipoGraoId;
        this.placa = placa;
        this.pesoBrutoEstabilizado = pesoBrutoEstabilizado;
        this.pesoLiquido = pesoLiquido;
        this.dataHoraPesagem = dataHoraPesagem;
        this.custoCarga = custoCarga;
        this.statusEstabilidade = statusEstabilidade;
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
    public UUID getTransacaoId() { return transacaoId; }
    public UUID getBalancaId() { return balancaId; }
    public UUID getCaminhaoId() { return caminhaoId; }
    public UUID getTipoGraoId() { return tipoGraoId; }
    public String getPlaca() { return placa; }
    public BigDecimal getPesoBrutoEstabilizado() { return pesoBrutoEstabilizado; }
    public BigDecimal getPesoLiquido() { return pesoLiquido; }
    public LocalDateTime getDataHoraPesagem() { return dataHoraPesagem; }
    public BigDecimal getCustoCarga() { return custoCarga; }
    public StatusPesagem getStatusEstabilidade() { return statusEstabilidade; }
    public String getObservacao() { return observacao; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
