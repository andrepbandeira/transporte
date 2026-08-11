package transporte.desafio.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Configuracao de seguranca.
 * <ul>
 *   <li>Endpoints de pesagem (ESP32) protegidos pelo header X-Balance-Token.</li>
 *   <li>Demais endpoints protegidos por JWT (OAuth2 resource server HS256).</li>
 *   <li>Login e Swagger publicos.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final String balanceToken;
    private final String jwtSecret;

    public SecurityConfig(@Value("${app.balance-token:}") String balanceToken,
                          @Value("${app.security.jwt.secret:}") String jwtSecret) {
        this.balanceToken = balanceToken;
        this.jwtSecret = jwtSecret;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/pesagens").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .headers(h -> h.frameOptions(f -> f.sameOrigin()));

        if (balanceToken != null && !balanceToken.isBlank()) {
            http.addFilterBefore(new BalanceTokenFilter(balanceToken),
                    UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] key = (jwtSecret == null || jwtSecret.isBlank())
                ? "chave-desenvolvimento-transporte-2026".getBytes(StandardCharsets.UTF_8)
                : jwtSecret.getBytes(StandardCharsets.UTF_8);
        SecretKey secretKey = new SecretKeySpec(key, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    private static final class BalanceTokenFilter extends OncePerRequestFilter {

        private final String esperado;

        BalanceTokenFilter(String esperado) {
            this.esperado = esperado;
        }

        @Override
        protected boolean shouldNotFilter(HttpServletRequest request) {
            return !"/api/v1/pesagens".equals(request.getRequestURI());
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain)
                throws ServletException, IOException {
            String token = request.getHeader("X-Balance-Token");
            if (token == null || !token.equals(esperado)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"X-Balance-Token invalido\"}");
                return;
            }
            filterChain.doFilter(request, response);
        }
    }
}
