package transporte.desafio.domain.model;

import transporte.desafio.domain.enums.StatusTransacao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Ciclo de vida de um transporte: inicio (partida da filial) -> pesagem
 * na doca -> fim (descarregado). Uma transacao pode ter varias pesagens.
 */
public class TransacaoTransporte {

    private final UUID id;
    private final UUID caminhaoId;
    private final UUID filialId;
    private final UUID tipoGraoId;
    private final UUID balancaId;
    private final LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private StatusTransacao status;
    private String observacao;
    private final LocalDateTime createdAt;
    private final List<Pesagem> pesagems = new ArrayList<>();

    public TransacaoTransporte(UUID id, UUID caminhaoId, UUID filialId, UUID tipoGraoId,
                               UUID balancaId, LocalDateTime dataInicio, LocalDateTime dataFim,
                               StatusTransacao status, String observacao, LocalDateTime createdAt) {
        this.id = Objects.requireNonNullElseGet(id, UUID::randomUUID);
        this.caminhaoId = Objects.requireNonNull(caminhaoId, "caminhaoId é obrigatório");
        this.filialId = Objects.requireNonNull(filialId, "filialId é obrigatório");
        this.tipoGraoId = Objects.requireNonNull(tipoGraoId, "tipoGraoId é obrigatório");
        this.balancaId = balancaId;
        this.dataInicio = Objects.requireNonNull(dataInicio, "dataInicio é obrigatório");
        this.dataFim = dataFim;
        this.status = Objects.requireNonNull(status, "status é obrigatório");
        this.observacao = observacao;
        this.createdAt = Objects.requireNonNullElse(createdAt, LocalDateTime.now());
    }

    public static TransacaoTransporte iniciar(UUID caminhaoId, UUID filialId, UUID tipoGraoId, UUID balancaId) {
        return new TransacaoTransporte(
                UUID.randomUUID(), caminhaoId, filialId, tipoGraoId, balancaId,
                LocalDateTime.now(), null, StatusTransacao.EM_ANDAMENTO, null, LocalDateTime.now());
    }

    // Mutators ----
    public void finalizar(String observacao) {
        this.status = StatusTransacao.FINALIZADA;
        this.dataFim = LocalDateTime.now();
        this.observacao = observacao;
    }

    public void cancelar(String observacao) {
        this.status = StatusTransacao.CANCELADA;
        this.dataFim = LocalDateTime.now();
        this.observacao = observacao;
    }

    public void adicionarPesagem(Pesagem pesagem) {
        this.pesagems.add(pesagem);
    }

    // Getters ----
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
    public List<Pesagem> getPesagems() { return Collections.unmodifiableList(pesagems); }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransacaoTransporte that)) return false;
        return Objects.equals(id, that.id);
    }
    @Override public int hashCode() { return Objects.hash(id); }
}
