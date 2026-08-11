package transporte.desafio.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * DTO para criar uma nova Balanca.
 *
 * @param codigo   identificador unico da balanca
 * @param password senha de autenticacao (sera codificada em BCrypt)
 * @param nome     nome amigavel
 * @param filialId UUID da filial a qual a balanca pertence
 */
public record BalancaRequest(
        @NotBlank(message = "codigo é obrigatório")
        @Size(max = 100)
        String codigo,

        @NotBlank(message = "password é obrigatório")
        String password,

        @NotBlank(message = "nome é obrigatório")
        @Size(max = 255)
        String nome,

        @NotNull(message = "filialId é obrigatório")
        UUID filialId
) {
}
