package transporte.desafio.application.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO para iniciar uma nova transacao de transporte.
 *
 * @param caminhaoId UUID do caminhão
 * @param filialId   UUID da filial origem/destino
 * @param tipoGraoId UUID do tipo de grão
 * @param balancaId  UUID da balanca (opcional, null = usar a default da filial)
 */
public record IniciarTransacaoRequest(
        @NotNull(message = "caminhaoId é obrigatório")
        UUID caminhaoId,

        @NotNull(message = "filialId é obrigatório")
        UUID filialId,

        @NotNull(message = "tipoGraoId é obrigatório")
        UUID tipoGraoId,

        UUID balancaId
) {
}
