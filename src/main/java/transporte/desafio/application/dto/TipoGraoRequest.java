package transporte.desafio.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO para criar um novo Tipo de Grão.
 *
 * @param nome                      nome do grão (ex: Soja, Milho)
 * @param precoCompraPorTonelada    preco de compra em R$/ton
 */
public record TipoGraoRequest(
        @NotBlank(message = "nome é obrigatório")
        @Size(max = 255)
        String nome,

        @NotNull(message = "precoCompraPorTonelada é obrigatório")
        @DecimalMin(value = "0.0", inclusive = true, message = "preco nao pode ser negativo")
        BigDecimal precoCompraPorTonelada
) {
}
