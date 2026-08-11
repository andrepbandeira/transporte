package transporte.desafio.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Estoque disponivel de um tipo de grao na doca, exposto via API.
 *
 * @param id                    UUID da doca
 * @param tipoGraoId            UUID do tipo de grao
 * @param tipoGrao              nome do tipo de grao
 * @param pesoDisponivelKg      saldo disponivel, em kg
 * @param precoVendaPorTonelada preco de venda estimado (preco compra * (1 + margem))
 */
public record DocaEstoqueResponse(
        UUID id,
        UUID tipoGraoId,
        String tipoGrao,
        BigDecimal pesoDisponivelKg,
        BigDecimal precoVendaPorTonelada
) {
}