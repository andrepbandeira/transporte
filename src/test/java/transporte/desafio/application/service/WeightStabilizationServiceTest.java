package transporte.desafio.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import transporte.desafio.application.dto.ScaleStabilizedEvent;
import transporte.desafio.application.dto.WeightReadingDto;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class WeightStabilizationServiceTest {

    private WeightStabilizationService service;

    @BeforeEach
    void setUp() {
        // bufferSize = 5, maxVariation = 5.0, minTruckWeight = 1000.0
        service = new WeightStabilizationService(5, 5.0, 1000.0);
    }

    private WeightReadingDto reading(String scaleId, String plate, double weight) {
        return new WeightReadingDto(scaleId, plate, BigDecimal.valueOf(weight));
    }

    @Test
    @DisplayName("retorna vazio quando o peso for inferior ao peso minimo de caminhao e limpa buffer")
    void testPesoAbaixoDoMinimoResetaBuffer() {
        String scale = "SCALE-1";
        String plate = "ABC-1234";

        // Adiciona 3 leituras validas
        service.processReading(reading(scale, plate, 1500.0));
        service.processReading(reading(scale, plate, 1500.0));
        service.processReading(reading(scale, plate, 1500.0));

        // Envia leitura abaixo de 1000kg
        Optional<ScaleStabilizedEvent> result = service.processReading(reading(scale, plate, 500.0));
        assertThat(result).isEmpty();

        // Envia mais 4 leituras validas (total 4 apos reset, buffer size = 5) -> nao deve estabilizar ainda
        for (int i = 0; i < 4; i++) {
            assertThat(service.processReading(reading(scale, plate, 1500.0))).isEmpty();
        }

        // A quinta leitura apos o reset deve estabilizar
        Optional<ScaleStabilizedEvent> stabilized = service.processReading(reading(scale, plate, 1500.0));
        assertThat(stabilized).isPresent();
        assertThat(stabilized.get().averageWeight()).isEqualByComparingTo(BigDecimal.valueOf(1500.0));
    }

    @Test
    @DisplayName("estabiliza quando buffer atinge tamanho e variacao e <= 5.0 kg")
    void testEstabilizaComVariacaoAceitavel() {
        String scale = "SCALE-1";
        String plate = "ABC-1234";

        assertThat(service.processReading(reading(scale, plate, 10000.0))).isEmpty();
        assertThat(service.processReading(reading(scale, plate, 10002.0))).isEmpty();
        assertThat(service.processReading(reading(scale, plate, 10001.0))).isEmpty();
        assertThat(service.processReading(reading(scale, plate, 10003.0))).isEmpty();

        Optional<ScaleStabilizedEvent> event = service.processReading(reading(scale, plate, 10000.0));
        assertThat(event).isPresent();
        assertThat(event.get().scaleId()).isEqualTo(scale);
        assertThat(event.get().plate()).isEqualTo(plate);
        // Media de 10000.0 + 10002.0 + 10001.0 + 10003.0 + 10000.0 = 50006.0 / 5 = 10001.20
        assertThat(event.get().averageWeight()).isEqualByComparingTo("10001.20");
    }

    @Test
    @DisplayName("nao estabiliza se variacao no buffer exceder 5.0 kg")
    void testNaoEstabilizaComVariacaoAlta() {
        String scale = "SCALE-1";
        String plate = "ABC-1234";

        service.processReading(reading(scale, plate, 10000.0));
        service.processReading(reading(scale, plate, 10000.0));
        service.processReading(reading(scale, plate, 10000.0));
        service.processReading(reading(scale, plate, 10000.0));

        // 10006.0 - 10000.0 = 6.0 > 5.0
        Optional<ScaleStabilizedEvent> event = service.processReading(reading(scale, plate, 10006.0));
        assertThat(event).isEmpty();
    }

    @Test
    @DisplayName("ignora leituras para placa ja processada (idempotencia)")
    void testIgnoraPlacaJaProcessada() {
        String scale = "SCALE-1";
        String plate = "ABC-1234";

        for (int i = 0; i < 5; i++) {
            service.processReading(reading(scale, plate, 10000.0));
        }

        // Leitura posterior para a mesma placa/balanca
        Optional<ScaleStabilizedEvent> reprocessed = service.processReading(reading(scale, plate, 10000.0));
        assertThat(reprocessed).isEmpty();
    }

    @Test
    @DisplayName("limpa buffer ao trocar de placa na mesma balanca")
    void testTrocaDePlacaLimpaBuffer() {
        String scale = "SCALE-1";

        service.processReading(reading(scale, "ABC-1234", 10000.0));
        service.processReading(reading(scale, "ABC-1234", 10000.0));

        // Troca placa
        service.processReading(reading(scale, "XYZ-9999", 12000.0));

        // Precisa de 5 leituras da nova placa para estabilizar
        for (int i = 0; i < 3; i++) {
            assertThat(service.processReading(reading(scale, "XYZ-9999", 12000.0))).isEmpty();
        }

        Optional<ScaleStabilizedEvent> event = service.processReading(reading(scale, "XYZ-9999", 12000.0));
        assertThat(event).isPresent();
        assertThat(event.get().plate()).isEqualTo("XYZ-9999");
    }
}
