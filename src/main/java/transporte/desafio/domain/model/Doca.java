package transporte.desafio.domain.model;

import transporte.desafio.domain.exception.RegraDeNegocioException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Estoque disponivel de um tipo de grao na doca.
 * Mantem apenas o saldo atual (atualizado por recebimento / venda parcial).
 */
public class Doca {

    private final UUID id;
    private final UUID tipoGraoId;
    private BigDecimal pesoDisponivel;
    private final LocalDateTime createdAt;

    public Doca(UUID id, UUID tipoGraoId, BigDecimal pesoDisponivel, LocalDateTime createdAt) {
        this.id = Objects.requireNonNullElseGet(id, UUID::randomUUID);
        this.tipoGraoId = Objects.requireNonNull(tipoGraoId, "tipoGraoId é obrigatório");
        this.pesoDisponivel = Objects.requireNonNullElse(pesoDisponivel, BigDecimal.ZERO);
        this.createdAt = Objects.requireNonNullElse(createdAt, LocalDateTime.now());
    }

    public static Doca nova(UUID tipoGraoId, BigDecimal pesoInicial) {
        return new Doca(UUID.randomUUID(), tipoGraoId, pesoInicial, LocalDateTime.now());
    }

    public void atualizarSaldo(BigDecimal novoPesoDisponivel) {
        this.pesoDisponivel = Objects.requireNonNullElse(novoPesoDisponivel, BigDecimal.ZERO);
    }

    public void adicionarEstoque(BigDecimal quantidade) {
        this.pesoDisponivel = this.pesoDisponivel.add(Objects.requireNonNullElse(quantidade, BigDecimal.ZERO));
    }

    public void reduzirEstoque(BigDecimal quantidade) {
        this.pesoDisponivel = this.pesoDisponivel.subtract(Objects.requireNonNullElse(quantidade, BigDecimal.ZERO));
    }

    /**
     * Vende uma quantidade de grao, reduzindo o saldo disponivel.
     * <p>
     * Atende {@code .clinerules/fluxo_execucao.md} item 10. Valida que a
     * quantidade e positiva e nao excede o saldo disponivel (venda parcial ou total).
     *
     * @param quantidade quantidade vendida em kg
     * @throws RegraDeNegocioException se a quantidade for invalida ou exceder o estoque
     */
    public void vender(BigDecimal quantidade) {
        if (quantidade == null || quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Quantidade de venda deve ser maior que zero");
        }
        if (quantidade.compareTo(this.pesoDisponivel) > 0) {
            throw new RegraDeNegocioException(
                    "Estoque insuficiente na doca: disponivel=" + this.pesoDisponivel
                            + " kg, solicitado=" + quantidade + " kg");
        }
        this.pesoDisponivel = this.pesoDisponivel.subtract(quantidade);
    }

    public UUID getId() { return id; }
    public UUID getTipoGraoId() { return tipoGraoId; }
    public BigDecimal getPesoDisponivel() { return pesoDisponivel; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Doca that)) return false;
        return Objects.equals(id, that.id);
    }
    @Override public int hashCode() { return Objects.hash(id); }
}
