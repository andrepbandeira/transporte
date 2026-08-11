package transporte.desafio.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentacao OpenAPI (Swagger).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        String scheme = "bearerAuth";
        SecurityScheme securityScheme = new SecurityScheme()
                .name(scheme)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");
        return new OpenAPI()
                .info(new Info()
                        .title("Sistema de Ingestao e Balancas de Graos")
                        .version("v1")
                        .description("API de recepcao de pesagens (ESP32) via Kafka, algoritmo de estabilizacao e margem dinamica."))
                .components(new Components().addSecuritySchemes(scheme, securityScheme))
                .addSecurityItem(new SecurityRequirement().addList(scheme));
    }
}
