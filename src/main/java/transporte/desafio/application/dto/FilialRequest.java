package transporte.desafio.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para criar uma nova Filial.
 */
public record FilialRequest(
        @NotBlank(message = "nome é obrigatório")
        @Size(max = 255)
        String nome,

        @Size(max = 255)
        String cidade,

        @Size(max = 100)
        String estado
) {
}
