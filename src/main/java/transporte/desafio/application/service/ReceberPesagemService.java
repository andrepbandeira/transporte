package transporte.desafio.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import transporte.desafio.application.dto.PesagemEvent;
import transporte.desafio.application.dto.PesagemRequest;
import transporte.desafio.application.ports.in.ReceberPesagemUseCase;
import transporte.desafio.application.ports.out.PesagemProducerPort;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Servico de ingestao: recebe leituras da balanca e despacha para Kafka.
 * <p>
 * A validacao HTTP e leve — apenas verifica o payload e envia ao Kafka.
 * Nenhum acesso ao banco de dados ocorre nesta thread.
 */
@Service
@Slf4j
public class ReceberPesagemService implements ReceberPesagemUseCase {

    private final PesagemProducerPort producer;

    public ReceberPesagemService(PesagemProducerPort producer) {
        this.producer = producer;
    }

    @Override
    @Async("pesagemExecutor")
    public void receber(PesagemRequest request) {
        try {
            UUID balancaId = UUID.fromString(request.id());
            PesagemEvent event = new PesagemEvent(
                    balancaId,
                    request.plate(),
                    request.weight(),
                    LocalDateTime.now());

            // Envia ao Kafka (fire-and-forget)
            producer.enviar(event);
            log.debug("Leitura despachada para Kafka: balanca={}, placa={}, peso={}",
                    request.id(), request.plate(), request.weight());
        } catch (IllegalArgumentException e) {
            log.warn("ID de balanca invalido recebido: {}", request.id());
        } catch (Exception e) {
            log.error("Erro ao despachar leitura para Kafka", e);
        }
    }
}
