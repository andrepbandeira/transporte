package transporte.desafio.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Unidade operacional da empresa. Uma filial pode ter varias balancas
 * e origem/destino de transacoes de transporte.
 */
public class Filial {

    private final UUID id;
    private final String nome;
    private final String cidade;
    private final String estado;
    private final Boolean ativo;
    private final LocalDateTime createdAt;

    public Filial(UUID id, String nome, String cidade, String estado, Boolean ativo, LocalDateTime createdAt) {
        this.id = Objects.requireNonNullElseGet(id, UUID::randomUUID);
        this.nome = Objects.requireNonNull(nome, "nome é obrigatório");
        this.cidade = cidade;
        this.estado = estado;
        this.ativo = Objects.requireNonNullElse(ativo, true);
        this.createdAt = Objects.requireNonNullElse(createdAt, LocalDateTime.now());
    }

    // Factory methods ----
    public static Filial novo(String nome, String cidade, String estado) {
        return new Filial(UUID.randomUUID(), nome, cidade, estado, true, LocalDateTime.now());
    }

    // Getters ----
    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public String getCidade() { return cidade; }
    public String getEstado() { return estado; }
    public Boolean getAtivo() { return ativo; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Filial that)) return false;
        return Objects.equals(id, that.id);
    }
    @Override public int hashCode() { return Objects.hash(id); }
}
