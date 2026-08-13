package transporte.desafio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import transporte.desafio.domain.service.CalculadoraMargemDinamica;

import java.math.BigDecimal;

/**
 * Beans de dominio e infraestrutura compartilhada.
 */
@Configuration
public class ApplicationConfig {

    @Bean
    public CalculadoraMargemDinamica calculadoraMargemDinamica(
            @org.springframework.beans.factory.annotation.Value("${app.margin.min:0.05}") double min,
            @org.springframework.beans.factory.annotation.Value("${app.margin.max:0.20}") double max,
            @org.springframework.beans.factory.annotation.Value("${app.margin.capacidade-ton:1000}") BigDecimal capacidade) {
        return new CalculadoraMargemDinamica(min, max, capacidade);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
