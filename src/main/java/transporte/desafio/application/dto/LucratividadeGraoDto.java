package transporte.desafio.application.dto;

import java.math.BigDecimal;

/**
 * Relatorio de lucratividade media por tipo de grao.
 *
 * @param tipoGraoNome        nome do tipo de grao
 * @param precoCompraMedio    preco medio de compra (R$/ton)
 * @param precoVendaMedio     preco medio de venda com margem dinamica (R$/ton)
 * @param margemMedia         margem media aplicada (fracao, ex: 0.10 = 10%)
 * @param lucroMedioPorTon    lucro medio por tonelada (R$/ton)
 * @param totalPesagens       quantidade de pesagens consolidadas
 */
public record LucratividadeGraoDto(
        String tipoGraoNome,
        BigDecimal precoCompraMedio,
        BigDecimal precoVendaMedio,
        BigDecimal margemMedia,
        BigDecimal lucroMedioPorTon,
        Long totalPesagens
) {
}
