package transporte.desafio.application.ports.in;

import transporte.desafio.application.dto.PesagemRequest;

/**
 * Use case: receber leitura da balanca (camada de ingestao).
 * Envia o dado para Kafka de forma assincrona (fire-and-forget).
 */
public interface ReceberPesagemUseCase {

    /**
     * Processa uma leitura da balanca.
     * Valida rapidamente e despacha para Kafka sem bloquear a thread HTTP.
     *
     * @param request payload do ESP32
     */
    void receber(PesagemRequest request);
}
