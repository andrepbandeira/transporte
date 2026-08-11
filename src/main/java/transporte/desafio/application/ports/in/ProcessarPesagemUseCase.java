package transporte.desafio.application.ports.in;

import transporte.desafio.application.dto.PesagemEvent;

/**
 * Use case: processar leitura da balanca (consumidor Kafka).
 * Executa validacoes, algoritmo de estabilizacao e persiste a pesagem.
 */
public interface ProcessarPesagemUseCase {

    /**
     * Processa uma leitura da balanca vinda do Kafka.
     *
     * @param event leitura a processar
     */
    void processar(PesagemEvent event);
}
