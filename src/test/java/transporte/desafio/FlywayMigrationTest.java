package transporte.desafio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica que as migracoes Flyway foram aplicadas e o schema existe.
 */
@SpringBootTest
@ActiveProfiles("test")
class FlywayMigrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("todas as tabelas do dominio existem apos a migracao")
    void todasAsTabelasExistem() {
        List<String> tabelas = List.of(
                "filial", "tipo_grao", "caminhao", "balanca",
                "transacao_transporte", "doca", "pesagem");

        for (String tabela : tabelas) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE UPPER(TABLE_NAME) = ? AND TABLE_SCHEMA = 'PUBLIC'",
                    Integer.class, tabela.toUpperCase());
            assertThat(count)
                    .as("tabela %s deve existir", tabela)
                    .isEqualTo(1);
        }
    }

    @Test
    @DisplayName("flyway_schema_history registra as migracoes aplicadas")
    void flywayHistoryRegistrado() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE success = TRUE",
                Integer.class);
        assertThat(count).isGreaterThanOrEqualTo(3); // V1, V2, V3
    }
}
