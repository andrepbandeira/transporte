package transporte.desafio.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO para registrar uma venda parcial ou total de grao na doca.
 *
 * @param tipoGraoId  UUID do tipo de grao vendido
 * @param quantidadeKg quantidade vendida, em kg
 */
public record VendaRequest(
        @NotNull(message = "tipoGraoId é obrigatório")
        UUID tipoGraoId,

        @NotNull(message = "quantidadeKg é obrigatório")
        @DecimalMin(value = "0.01", message = "quantidadeKg deve ser maior que zero")
        BigDecimal quantidadeKg
) {
}