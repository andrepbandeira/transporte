package transporte.desafio.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Caminhao cadastrado. A tara (peso de fabrica) e descontada do peso bruto
 * para obter o peso liquido da carga.
 */
public class Caminhao {

    private final UUID id;
    private final String placa;
    private final BigDecimal tara;
    private final String descricao;
    private final Boolean ativo;
    private final LocalDateTime createdAt;

    public Caminhao(UUID id, String placa, BigDecimal tara, String descricao, Boolean ativo, LocalDateTime createdAt) {
        this.id = Objects.requireNonNullElseGet(id, UUID::randomUUID);
        this.placa = Objects.requireNonNull(placa, "placa é obrigatória");
        this.tara = Objects.requireNonNullElse(tara, BigDecimal.ZERO);
        this.descricao = descricao;
        this.ativo = Objects.requireNonNullElse(ativo, true);
        this.createdAt = Objects.requireNonNullElse(createdAt, LocalDateTime.now());
    }

    public static Caminhao novo(String placa, BigDecimal tara, String descricao) {
        return new Caminhao(UUID.randomUUID(), placa, tara, descricao, true, LocalDateTime.now());
    }

    public UUID getId() { return id; }
    public String getPlaca() { return placa; }
    public BigDecimal getTara() { return tara; }
    public String getDescricao() { return descricao; }
    public Boolean getAtivo() { return ativo; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Caminhao that)) return false;
        return Objects.equals(id, that.id);
    }
    @Override public int hashCode() { return Objects.hash(id); }
}
