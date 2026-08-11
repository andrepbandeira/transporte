package transporte.desafio.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Balanca fisica instalada em uma filial.
 * <p>
 * O {@code codigo} identifica a balanca no payload do ESP32.
 * A {@code password} autentica a balanca no consumidor Kafka.
 * O {@code nome} e um rótulo humano-friendly.
 */
public class Balanca {

    private final UUID id;
    private final String codigo;
    private final String password;
    private final String nome;
    private final UUID filialId;
    private final Boolean ativa;
    private final LocalDateTime createdAt;

    public Balanca(UUID id, String codigo, String password, String nome, UUID filialId, Boolean ativa, LocalDateTime createdAt) {
        this.id = Objects.requireNonNullElseGet(id, UUID::randomUUID);
        this.codigo = Objects.requireNonNull(codigo, "codigo é obrigatório");
        this.password = Objects.requireNonNull(password, "password é obrigatório");
        this.nome = Objects.requireNonNull(nome, "nome é obrigatório");
        this.filialId = Objects.requireNonNull(filialId, "filialId é obrigatório");
        this.ativa = Objects.requireNonNullElse(ativa, true);
        this.createdAt = Objects.requireNonNullElse(createdAt, LocalDateTime.now());
    }

    public static Balanca nova(String codigo, String password, String nome, UUID filialId) {
        return new Balanca(UUID.randomUUID(), codigo, password, nome, filialId, true, LocalDateTime.now());
    }

    public UUID getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getPassword() { return password; }
    public String getNome() { return nome; }
    public UUID getFilialId() { return filialId; }
    public Boolean getAtiva() { return ativa; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Balanca that)) return false;
        return Objects.equals(id, that.id);
    }
    @Override public int hashCode() { return Objects.hash(id); }
}
