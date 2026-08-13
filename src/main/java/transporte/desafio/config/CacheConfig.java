package transporte.desafio.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuracao de cache via Caffeine.
 * <p>
 * Implementa o mesmo padrao do {@code CacheConfig} de referencia:
 * um {@link CaffeineCacheManager} nomeado {@code "truck-tares"} que armazena
 * em cache a tara dos caminhoes por placa, evitando buscas repetidas no banco
 * durante o processamento de leituras de balanca.
 * </p>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Nome do cache que armazena a tara do caminhão por placa.
     */
    public static final String TRUCK_TARES_CACHE = "truck-tares";

    @Bean
    public CaffeineCacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(TRUCK_TARES_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(12, TimeUnit.HOURS)
                .maximumSize(5000));
        return cacheManager;
    }
}
