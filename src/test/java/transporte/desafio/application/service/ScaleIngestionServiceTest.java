package transporte.desafio.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import transporte.desafio.application.dto.WeightReadingDto;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Testes do gate de tara do ScaleIngestionService (mesma logica do
 * ScaleIngestionService de referencia: tara * 0.90).
 * <p>
 * Verifica tanto o metodo que recebe a tara explicitamente
 * ({@link ScaleIngestionService#isReadingValid(WeightReadingDto, BigDecimal)})
 * quanto o metodo que resolve a tara via cache pelo {@link TruckTareService}.
 */
@ExtendWith(MockitoExtension.class)
class ScaleIngestionServiceTest {

    private static final BigDecimal TARA = BigDecimal.valueOf(5000);
    private static final BigDecimal MINIMO = BigDecimal.valueOf(4500); // 5000 * 0.90
    private static final String PLACA = "ABC-1234";

    @Mock
    private TruckTareService truckTareService;

    private ScaleIngestionService service;

    @BeforeEach
    void setUp() {
        service = new ScaleIngestionService(truckTareService);
    }

    private WeightReadingDto reading(double weight) {
        return new WeightReadingDto("scale-1", PLACA, BigDecimal.valueOf(weight));
    }

    // ===== isReadingValid(reading, tare) - tara via parametro (backward compat) =====

    @Test
    @DisplayName("aceita leitura acima de tara * 0.90")
    void aceitaLeituraAcimaDaTaraMinima() {
        assertThat(service.isReadingValid(reading(TARA.doubleValue() + 2000), TARA)).isTrue();
    }

    @Test
    @DisplayName("aceita leitura exatamente igual a tara * 0.90")
    void aceitaLeituraIgualAoMinimo() {
        assertThat(service.isReadingValid(reading(MINIMO.doubleValue()), TARA)).isTrue();
    }

    @Test
    @DisplayName("descarta leitura abaixo de tara * 0.90")
    void descartaLeituraAbaixoDaTaraMinima() {
        assertThat(service.isReadingValid(reading(MINIMO.doubleValue() - 1), TARA)).isFalse();
        assertThat(service.isReadingValid(reading(1.0), TARA)).isFalse();
    }

    @Test
    @DisplayName("descarta leitura quando peso e nulo")
    void descartaLeituraComPesoNulo() {
        WeightReadingDto nula = new WeightReadingDto("scale-1", PLACA, null);
        assertThat(service.isReadingValid(nula, TARA)).isFalse();
    }

    @Test
    @DisplayName("descarta leitura quando tara e nula ou leitura e nula")
    void descartaLeituraComTaraNula() {
        assertThat(service.isReadingValid(reading(10000.0), null)).isFalse();
        assertThat(service.isReadingValid(null, TARA)).isFalse();
    }

    // ===== isReadingValid(reading) - tara via TruckTareService (cache truck-tares) =====

    @Test
    @DisplayName("aceita leitura quando tara resolvida via cache retorna valor valido")
    void aceitaLeituraViaCacheTaraResolvida() {
        when(truckTareService.getTareByPlate(PLACA)).thenReturn(TARA);

        assertThat(service.isReadingValid(reading(TARA.doubleValue() + 2000))).isTrue();
    }

    @Test
    @DisplayName("descarta leitura quando tara via cache indica peso abaixo do minimo")
    void descartaLeituraViaCacheTaraAbaixoDoMinimo() {
        when(truckTareService.getTareByPlate(PLACA)).thenReturn(TARA);

        assertThat(service.isReadingValid(reading(4000.0))).isFalse();
    }

    @Test
    @DisplayName("usa tara default quando caminhao nao cadastrado")
    void usaTaraDefaultQuandoCaminhaoNaoCadastrado() {
        // TruckTareService retorna DEFAULT_MIN_TARE = 8000 quando nao encontra o caminhao
        when(truckTareService.getTareByPlate(PLACA)).thenReturn(BigDecimal.valueOf(8000.0));

        // tara 8000 -> minimo = 7200; peso 10000 > 7200 -> valido
        assertThat(service.isReadingValid(reading(10000.0))).isTrue();
        // peso 5000 < 7200 -> invalido
        assertThat(service.isReadingValid(reading(5000.0))).isFalse();
    }

    @Test
    @DisplayName("descarta leitura quando peso e nulo mesmo com tara via cache")
    void descartaLeituraNulaViaCache() {
        WeightReadingDto nula = new WeightReadingDto("scale-1", PLACA, null);
        assertThat(service.isReadingValid(nula)).isFalse();
    }

    @Test
    @DisplayName("descarta leitura nula total via cache")
    void descartaLeituraNulaTotalViaCache() {
        assertThat(service.isReadingValid(null)).isFalse();
    }
}
