package transporte.desafio.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO para criar um novo Caminhao.
 *
 * @param placa     placa do caminhão (única)
 * @param tara      peso de fabrica em kg
 * @param descricao descricao opcional
 */
public record CaminhaoRequest(
        @NotBlank(message = "placa é obrigatória")
        @Size(max = 20)
        String placa,

        @NotNull(message = "tara é obrigatória")
        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal tara,

        @Size(max = 255)
        String descricao
) {
}
