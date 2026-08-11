package transporte.desafio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuracao de processamento assincrono (pool de threads dedicado).
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "pesagemExecutor")
    public Executor pesagemExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("pesagem-produtor-");
        executor.initialize();
        return executor;
    }
}
