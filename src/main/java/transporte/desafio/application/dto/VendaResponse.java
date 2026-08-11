package transporte.desafio.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Resultado de uma venda de grao na doca.
 *
 * @param tipoGraoId             UUID do tipo de grao vendido
 * @param tipoGrao               nome do tipo de grao
 * @param quantidadeVendidaKg    quantidade vendida, em kg
 * @param saldoRestanteKg        saldo restante disponivel na doca, em kg
 * @param precoVendaPorTonelada  preco de venda por tonelada aplicado
 * @param valorVenda             valor total da venda, em R$
 */
public record VendaResponse(
        UUID tipoGraoId,
        String tipoGrao,
        BigDecimal quantidadeVendidaKg,
        BigDecimal saldoRestanteKg,
        BigDecimal precoVendaPorTonelada,
        BigDecimal valorVenda
) {
}