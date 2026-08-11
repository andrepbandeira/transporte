package transporte.desafio.adapter.outbound.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import transporte.desafio.application.dto.PesagemEvent;
import transporte.desafio.application.ports.out.PesagemProducerPort;

/**
 * Produtor Kafka (fire-and-forget) para o topico de pesagens.
 */
@Component
@Slf4j
public class PesagemKafkaProducer implements PesagemProducerPort {

    private final KafkaTemplate<String, PesagemEvent> kafkaTemplate;
    private final String topico;

    public PesagemKafkaProducer(KafkaTemplate<String, PesagemEvent> kafkaTemplate,
                                @org.springframework.beans.factory.annotation.Value("${app.balance-topic:pesagens}") String topico) {
        this.kafkaTemplate = kafkaTemplate;
        this.topico = topico;
    }

    @Override
    public void enviar(PesagemEvent event) {
        try {
            kafkaTemplate.send(topico, event.placa(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Erro ao publicar pesagem para o topico {}: {}",
                                    topico, ex.getMessage());
                        } else {
                            log.debug("Pesagem publicada: topico={}, offset={}",
                                    topico, result != null ? result.getRecordMetadata().offset() : null);
                        }
                    });
        } catch (Exception e) {
            log.error("Falha ao enviar pesagem para Kafka: {}", e.getMessage());
        }
    }
}
