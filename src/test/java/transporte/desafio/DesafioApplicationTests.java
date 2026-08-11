package transporte.desafio;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifica que o contexto Spring carrega corretamente com o perfil de teste (H2).
 */
@SpringBootTest
@ActiveProfiles("test")
class DesafioApplicationTests {

    @Test
    void contextLoads() {
        // Se o contexto carregar, o teste passa.
    }
}
