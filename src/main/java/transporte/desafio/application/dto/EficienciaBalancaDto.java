package transporte.desafio.application.dto;

/**
 * Eficiencia operacional da balanca: tempo medio de estabilizacao
 * e quantidade de caminhoes atendidos.
 *
 * @param balancaNome           nome da balanca
 * @param tempoMedioEstabilizacaoMs tempo medio para estabilizar (ms)
 * @param quantidadeCaminhoesAtendidos numero de caminhoes processados
 */
public record EficienciaBalancaDto(
        String balancaNome,
        Long tempoMedioEstabilizacaoMs,
        Long quantidadeCaminhoesAtendidos
) {
}
