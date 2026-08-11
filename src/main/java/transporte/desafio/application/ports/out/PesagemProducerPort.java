package transporte.desafio.application.ports.out;

import transporte.desafio.application.dto.PesagemEvent;

/**
 * Porta de saida: produtor de eventos para o Kafka.
 * Envia leituras da balanca para processamento assincrono.
 */
public interface PesagemProducerPort {

    /**
     * Envia a leitura para o topico Kafka de forma fire-and-forget.
     *
     * @param event leitura da balanca
     */
    void enviar(PesagemEvent event);
}
