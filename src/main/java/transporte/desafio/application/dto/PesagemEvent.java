package transporte.desafio.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento enviado ao Kafka pelo produtor.
 * Representa uma unica leitura da balanca para processamento assincrono.
 *
 * @param balancaId  UUID da balanca que gerou a leitura
 * @param placa      placa do caminhão (pode ser vazia quando a balanca esta livre)
 * @param pesoAtual  peso bruto em kg
 * @param instante   momento da leitura
 */
public record PesagemEvent(
        UUID balancaId,
        String placa,
        Double pesoAtual,
        Instant instante
) {
}
