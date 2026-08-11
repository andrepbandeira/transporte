package transporte.desafio.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Tipo de grão transportado (ex: Soja, Milho).
 * Carrega o preco de compra por tonelada — base para custo da carga e margem.
 */
public class TipoGrao {

    private final UUID id;
    private final String nome;
    private final BigDecimal precoCompraPorTonelada;
    private final Boolean ativo;
    private final LocalDateTime createdAt;

    public TipoGrao(UUID id, String nome, BigDecimal precoCompraPorTonelada, Boolean ativo, LocalDateTime createdAt) {
        this.id = Objects.requireNonNullElseGet(id, UUID::randomUUID);
        this.nome = Objects.requireNonNull(nome, "nome é obrigatório");
        this.precoCompraPorTonelada = Objects.requireNonNull(precoCompraPorTonelada, "precoCompraPorTonelada é obrigatório");
        this.ativo = Objects.requireNonNullElse(ativo, true);
        this.createdAt = Objects.requireNonNullElse(createdAt, LocalDateTime.now());
    }

    public static TipoGrao novo(String nome, BigDecimal precoCompraPorTonelada) {
        return new TipoGrao(UUID.randomUUID(), nome, precoCompraPorTonelada, true, LocalDateTime.now());
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public BigDecimal getPrecoCompraPorTonelada() { return precoCompraPorTonelada; }
    public Boolean getAtivo() { return ativo; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TipoGrao that)) return false;
        return Objects.equals(id, that.id);
    }
    @Override public int hashCode() { return Objects.hash(id); }
}
