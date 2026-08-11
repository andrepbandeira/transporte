package transporte.desafio.domain.model;

import transporte.desafio.domain.enums.StatusPesagem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Pesagem consolidada — persistida apenas quando o peso esta estabilizado.
 * Representa o registro final do ciclo de estabilizacao para uma transacao.
 */
public class Pesagem {

    private final UUID id;
    private final UUID transacaoId;
    private final UUID balancaId;
    private final UUID caminhaoId;
    private final UUID tipoGraoId;
    private final String placa;
    private final BigDecimal pesoBrutoEstabilizado;
    private final BigDecimal pesoLiquido;
    private final LocalDateTime dataHoraPesagem;
    private final BigDecimal custoCarga;
    private final StatusPesagem statusEstabilidade;
    private final String observacao;
    private final LocalDateTime createdAt;

    public Pesagem(UUID id, UUID transacaoId, UUID balancaId, UUID caminhaoId,
                   UUID tipoGraoId, String placa, BigDecimal pesoBrutoEstabilizado,
                   BigDecimal pesoLiquido, LocalDateTime dataHoraPesagem,
                   BigDecimal custoCarga, StatusPesagem statusEstabilidade,
                   String observacao, LocalDateTime createdAt) {
        this.id = Objects.requireNonNullElseGet(id, UUID::randomUUID);
        this.transacaoId = Objects.requireNonNull(transacaoId, "transacaoId é obrigatório");
        this.balancaId = Objects.requireNonNull(balancaId, "balancaId é obrigatório");
        this.caminhaoId = caminhaoId;
        this.tipoGraoId = Objects.requireNonNull(tipoGraoId, "tipoGraoId é obrigatório");
        this.placa = Objects.requireNonNull(placa, "placa é obrigatória");
        this.pesoBrutoEstabilizado = Objects.requireNonNull(pesoBrutoEstabilizado, "pesoBrutoEstabilizado é obrigatório");
        this.pesoLiquido = Objects.requireNonNull(pesoLiquido, "pesoLiquido é obrigatório");
        this.dataHoraPesagem = Objects.requireNonNull(dataHoraPesagem, "dataHoraPesagem é obrigatório");
        this.custoCarga = custoCarga;
        this.statusEstabilidade = Objects.requireNonNull(statusEstabilidade, "statusEstabilidade é obrigatório");
        this.observacao = observacao;
        this.createdAt = Objects.requireNonNullElse(createdAt, LocalDateTime.now());
    }

    // Getters ----
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

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pesagem that)) return false;
        return Objects.equals(id, that.id);
    }
    @Override public int hashCode() { return Objects.hash(id); }
}
