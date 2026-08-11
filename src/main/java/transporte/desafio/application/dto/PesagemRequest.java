package transporte.desafio.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Payload do ESP32: identificador da balanca, placa do caminhão e peso atual.
 *
 * @param id     UUID da balanca (como string)
 * @param plate  placa do caminhão (vazio quando a balanca esta livre)
 * @param weight peso bruto capturado, em kg
 */
public record PesagemRequest(
        @NotBlank(message = "id (balanca) é obrigatório")
        @Size(max = 36, message = "id deve ser um UUID")
        String id,

        @Size(max = 20, message = "plate deve ter no máximo 20 caracteres")
        String plate,

        @NotNull(message = "weight é obrigatório")
        @PositiveOrZero(message = "weight não pode ser negativo")
        Double weight
) {
}
