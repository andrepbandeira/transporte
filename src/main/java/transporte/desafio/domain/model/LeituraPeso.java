package transporte.desafio.domain.model;

import java.time.Instant;

/**
 * Leitura bruta de peso recebida da balanca.
 * <p>
 * Imutavel — representa um unico ponto na serie temporal de leituras
 * enviadas pelo ESP32 a cada 100 ms.
 *
 * @param instante  momento da leitura (epoch-millis ou Instant)
 * @param pesoKg    peso bruto em kg capturado pelo sensor
 */
public record LeituraPeso(Instant instante, double pesoKg) {
}
