package transporte.desafio.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import transporte.desafio.application.dto.WeightReadingDto;

import java.math.BigDecimal;

/**
 * Implementa a mesma logica do {@code ScaleIngestionService} de referencia:
 * valida uma leitura da balanca contra a tara do caminhao e descarta (ignora)
 * leituras abaixo de {@code tara * TARE_TOLERANCE_FACTOR}.
 * <p>
 * A tara do caminhão é obtida via {@link TruckTareService#getTareByPlate(String)},
 * que armazena o resultado em cache no {@code CaffeineCacheManager("truck-tares")}.
 * Assim, durante a ingestao contínua de leituras (100ms), a busca da tara
 * ocorre apenas uma vez por placa na janela de validade do cache (12 horas).
 * </p>
 * <p>
 * Isso garante que apenas leituras com peso consistente com um caminhao sobre
 * a balanca sejam encaminhadas para o algoritmo de estabilizacao.
 * </p>
 */
@Service
@Slf4j
public class ScaleIngestionService {

    private static final BigDecimal TARE_TOLERANCE_FACTOR = BigDecimal.valueOf(0.90);

    private final TruckTareService truckTareService;

    public ScaleIngestionService(TruckTareService truckTareService) {
        this.truckTareService = truckTareService;
    }

    /**
     * Valida uma leitura usando o mesmo criterio de tolerancia do
     * {@code ScaleIngestionService} de referencia, obtendo a tara do caminhao
     * pela placa via {@link TruckTareService} (com cache "truck-tares").
     *
     * @param reading leitura da balanca (id, placa, peso)
     * @return {@code true} se a leitura é valida (peso >= tara * 0.90);
     *         {@code false} caso contrário (leitura a ser descartada)
     */
    public boolean isReadingValid(WeightReadingDto reading) {
        if (reading == null || reading.weight() == null) {
            return false;
        }

        BigDecimal tare = truckTareService.getTareByPlate(reading.plate());
        return isReadingValid(reading, tare);
    }

    /**
     * Valida uma leitura usando o mesmo criterio de tolerancia do
     * {@code ScaleIngestionService} de referencia.
     * <p>
     * Variante que recebe a tara explicitamente, sem atravessar o cache.
     * Útil para testes e para chamadores que já possuem a tara em mãos.
     * </p>
     *
     * @param reading leitura da balanca (id, placa, peso)
     * @param tare    tara do caminhao em kg
     * @return {@code true} se a leitura é valida (peso {@code >= tara * 0.90});
     *         {@code false} caso contrário (leitura a ser descartada)
     */
    public boolean isReadingValid(WeightReadingDto reading, BigDecimal tare) {
        if (reading == null || reading.weight() == null || tare == null) {
            return false;
        }

        BigDecimal minRequiredWeight = tare.multiply(TARE_TOLERANCE_FACTOR);

        if (reading.weight().compareTo(minRequiredWeight) < 0) {
            log.trace("Leitura descartada (abaixo da tara). Placa: {}, Peso: {}, Tara Minima: {}",
                    reading.plate(), reading.weight(), minRequiredWeight);
            return false;
        }

        return true;
    }
}
