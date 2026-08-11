package transporte.desafio.application.dto;

import java.math.BigDecimal;

/**
 * Volume total de grãos movimentados por filial e tipo de grão.
 *
 * @param filialNome   nome da filial
 * @param tipoGraoNome nome do tipo de grão
 * @param volumeToneladas volume movimentado em toneladas
 */
public record VolumeFilialDto(
        String filialNome,
        String tipoGraoNome,
        BigDecimal volumeToneladas
) {
}
