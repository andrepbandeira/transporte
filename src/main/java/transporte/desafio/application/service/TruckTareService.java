package transporte.desafio.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import transporte.desafio.application.ports.out.CaminhaoRepositoryPort;
import transporte.desafio.domain.model.Caminhao;

import java.math.BigDecimal;

/**
 * Servico responsavel por buscar a tara (peso em ordem de fabrica) de um
 * caminhão a partir da placa.
 * <p>
 * Implementa a mesma logica do {@code TruckTareService} de referencia:
 * a tara e armazenada em cache no {@code CaffeineCacheManager("truck-tares")}
 * para evitar buscas repetidas no banco de dados durante o ingestao de
 * leituras da balanca.
 * </p>
 * <p>
 * Quando o caminhao nao for encontrado, retorna uma tara padrao minima
 * ({@link #DEFAULT_MIN_TARE}) para garantir que o gate de tara do
 * {@link ScaleIngestionService} funcione mesmo sem cadastro previo.
 * </p>
 */
@Service
@Slf4j
public class TruckTareService {

    /**
     * Tara minima default (em kg) usada quando o caminhao nao esta cadastrado.
     * Valor alinhado ao padrao do projeto: 8000 kg.
     */
    private static final BigDecimal DEFAULT_MIN_TARE = BigDecimal.valueOf(8000.0);

    private final CaminhaoRepositoryPort caminhaoRepository;

    public TruckTareService(CaminhaoRepositoryPort caminhaoRepository) {
        this.caminhaoRepository = caminhaoRepository;
    }

    /**
     * Retorna a tara do caminhão para a placa informada.
     * <p>
     * O resultado é armazenado em cache utilizando o cache
     * {@code "truck-tares"} (gerenciado por {@link CaffeineCacheManager})
     * com chave sendo a própria placa. Assim, chamadas repetidas para a
     * mesma placa no periodo de validade do cache (12 horas) evitam acesso
     * ao banco de dados.
     * </p>
     *
     * @param plate placa do caminhão
     * @return tara do caminhão em kg, ou {@link #DEFAULT_MIN_TARE} se nao cadastrado
     */
    @Cacheable(value = "truck-tares", key = "#plate")
    public BigDecimal getTareByPlate(String plate) {
        log.info("BUSCA NO BANCO SQL: Carregando tara para a placa {}", plate);
        return caminhaoRepository.buscarPorPlaca(plate)
                .map(Caminhao::getTara)
                .orElse(DEFAULT_MIN_TARE);
    }
}
