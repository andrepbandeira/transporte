package transporte.desafio.adapter.inbound.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import transporte.desafio.application.dto.PesagemEvent;
import transporte.desafio.application.ports.in.ProcessarPesagemUseCase;
import transporte.desafio.domain.exception.RegraDeNegocioException;

/**
 * Consumidor Kafka: processa pesagens de forma assincrona.
 */
@Component
@Slf4j
public class PesagemKafkaConsumer {

    private final ProcessarPesagemUseCase processarPesagemUseCase;

    public PesagemKafkaConsumer(ProcessarPesagemUseCase processarPesagemUseCase) {
        this.processarPesagemUseCase = processarPesagemUseCase;
    }

    @KafkaListener(topics = "${app.balance-topic:pesagens}",
            groupId = "${spring.kafka.consumer.group-id:balancas-consumer-group}")
    public void consumir(PesagemEvent event) {
        try {
            processarPesagemUseCase.processar(event);
        } catch (RegraDeNegocioException e) {
            log.warn("Leitura ignorada: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao processar pesagem: {}", e.getMessage());
            throw e; // permite retentativa do Kafka
        }
    }
}
