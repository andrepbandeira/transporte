# JAva Standards

---

## 🛠️ Padrões de Código (Java 21 Standards)

### 1. Funcionalidades Modernas do Java
- **Records**: Use sempre `record` para DTOs, payloads de API, chaves de mensagens Kafka e estruturas de dados imutáveis.
- **Pattern Matching para Switch e Instanceof**: Escreva códigos de validação de tipo mais limpos usando correspondência de padrões.
- **Text Blocks**: Utilize blocos de texto (`"""`) para consultas SQL complexas, JSONs de teste ou templates de texto.
- **Sequenced Collections**: Use as novas interfaces de coleções (`LinkedHashSet`, `ArrayList`) quando a ordem dos elementos importar (ex: `.getFirst()`, `.getLast()`).

### 2. Boas Práticas Gerais
- **Imutabilidade**: Torne as variáveis de referência finais (`final`) sempre que possível.
- **Streams API**: Prefira abordagens declarativas com streams para manipulação de coleções, mantendo a legibilidade.
- **Tratamento de Exceções**: 
  - Nunca capture `Exception` genérica; use exceções de negócio específicas (ex: `EntidadeNaoEncontradaException`).
  - Não use exceções para controle de fluxo comum de regras de negócio.

---

## 🍃 Spring Boot 4.x Best Practices

### 1. Injeção de Dependência
- Use **Injeção via Construtor** em vez de `@Autowired` em campos.
- Aproveite a inicialização implícita do Spring (omitindo o `@Autowired` quando houver apenas um construtor).
- **Novo em Boot 4.x**: Use `@PrimaryConstructor` (preview em Java 21) para reduzir boilerplate em Records e Classes com múltiplos construtores.

### 2. Camada Web & REST
- Retorne respostas imutáveis encapsuladas em `ResponseEntity<T>`.
- Use a validação nativa do Spring (`@Valid`, `@NotNull`, `@Size`, `@NotBlank`) nos DTOs de entrada.
- Centralize o tratamento de erros globais utilizando um `@RestControllerAdvice` herdando de `ResponseEntityExceptionHandler`.
- **Novo em Boot 4.x**: Aproveite o suporte a **Virtual Threads** (Project Loom) configurando Tomcat para usar `VirtualThreadExecutor` e melhorar concorrência de I/O.

### 3. Camada de Persistência (Spring Data JPA)
- Use métodos derivados de consulta (`findByPlaca`) para buscas simples.
- Para consultas complexas ou relatórios, prefira consultas JPQL ou Nativas usando `@Query` com paginação (`Pageable`).
- Sempre mapeie relacionamentos de banco com carregamento preguiçoso (`FetchType.LAZY`) para evitar o problema de N+1 consultas.
- **PostgreSQL** é o banco de dados principal, habilitado via `docker-compose` para desenvolvimento e produção.
- **Novo em Boot 4.x**: Use **Projections** e **DTOs** com mapeamento automático via Spring Data para otimizar queries que retornam apenas campos específicos.

#### 3.1 Auditing Automático com @EnableJdbcAuditing

**Objetivo**: Rastrear automaticamente quando registros foram criados e modificados.

**Configuração Necessária**:

1. **Habilitar Auditing na Classe de Configuração**:
```java
@Configuration
@EnableJdbcAuditing(auditorAwareRef = "auditorProvider")  // Opcional: customizar o "quem"
public class PersistenceConfig {
    // Seu auditor provider (opcional)
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of("SISTEMA");  // ou user principal
    }
}
```

2. **Usar em Entidades JPA**:
```java
@Entity
@Table(name = "transacao")
public class TransacaoJpaEntity {
    
    @Id
    private String id;
    
    @Column(nullable = false)
    private String placa;
    
    @Column(nullable = false)
    private BigDecimal pesoLiquido;
    
    // ============ CAMPOS DE AUDITORIA AUTOMÁTICA ============
    @CreatedDate  // Preenchido automaticamente na criação
    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;
    
    @LastModifiedDate  // Preenchido automaticamente na atualização
    @Column(nullable = false)
    private LocalDateTime modificadoEm;
    
    // (Opcional) Rastrear quem criou/modificou
    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String criadoPor;
    
    @LastModifiedBy
    @Column(nullable = false)
    private String modificadoPor;
    
    // ... construtores, getters, setters
}
```

3. **SQL Migration (Flyway)**:
```sql
-- V2__add_audit_columns.sql
ALTER TABLE transacao ADD COLUMN criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL;
ALTER TABLE transacao ADD COLUMN modificado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL;
ALTER TABLE transacao ADD COLUMN criado_por VARCHAR(255) DEFAULT 'SISTEMA' NOT NULL;
ALTER TABLE transacao ADD COLUMN modificado_por VARCHAR(255) DEFAULT 'SISTEMA' NOT NULL;
```

4. **Domain Model (sem anotações Spring)**:
```java
public class Transacao {
    private String id;
    private String placa;
    private BigDecimal pesoLiquido;
    private LocalDateTime criadoEm;      // Preenchido via adapter
    private LocalDateTime modificadoEm;  // Preenchido via adapter
    
    // Constructor, getters
    public Transacao(String id, String placa, BigDecimal pesoLiquido, 
                     LocalDateTime criadoEm, LocalDateTime modificadoEm) {
        this.id = id;
        this.placa = placa;
        this.pesoLiquido = pesoLiquido;
        this.criadoEm = criadoEm;
        this.modificadoEm = modificadoEm;
    }
}
```

5. **Adapter (Converter JpaEntity → Domain)**:
```java
@Component
public class TransacaoRepositoryAdapter implements TransacaoRepositoryPort {
    
    private final TransacaoJpaRepository repository;
    
    public TransacaoRepositoryAdapter(TransacaoJpaRepository repository) {
        this.repository = repository;
    }
    
    @Override
    public Transacao salvar(Transacao transacao) {
        TransacaoJpaEntity entity = new TransacaoJpaEntity(
            transacao.getId(),
            transacao.getPlaca(),
            transacao.getPesoLiquido()
            // criadoEm e modificadoEm são preenchidos automaticamente por @CreatedDate/@LastModifiedDate
        );
        
        TransacaoJpaEntity salva = repository.save(entity);
        
        // Converter JPA Entity → Domain Model (incluindo audit fields)
        return toDomain(salva);
    }
    
    private Transacao toDomain(TransacaoJpaEntity entity) {
        return new Transacao(
            entity.getId(),
            entity.getPlaca(),
            entity.getPesoLiquido(),
            entity.getCriadoEm(),      // Agora preenchido
            entity.getModificadoEm()   // Agora preenchido
        );
    }
}
```

**Benefícios**:
- ✅ Sem necessidade de preencher `createdAt` manualmente
- ✅ Spring Data JPA gerencia automaticamente via `@CreatedDate`
- ✅ `@EnableJdbcAuditing` habilita esse recurso globalmente
- ✅ Rastreabilidade completa (quem criou, quando criou, quem modificou, quando modificou)
- ✅ Evita bugs de timestamp incorreto

### 4. Observabilidade e Monitoramento
- **Micrometer + OpenTelemetry**: Spring Boot 4.x integra nativamente observabilidade com métricas, traces e logs estruturados.
- Configure `management.endpoints.web.exposure.include` para expor endpoints de health, metrics e traces.
- Use `@Timed`, `@Counted` e `@RecordFailureTag` do Micrometer para instrumentalizar métodos críticos.

---

## 🚀 Funcionalidades Avançadas (Java 21 + Spring Boot 4.x)

### 1. Virtual Threads (Project Loom)
- **Novo em Java 21**: Use Virtual Threads para melhorar a concorrência de I/O sem overhead de threads nativas.
- Configure o Tomcat em `application.yaml`:
  ```yaml
  server:
    tomcat:
      threads:
        virtual:
          enabled: true
  ```
- Ideal para processamento de múltiplas requisições HTTP simultâneas do ESP32 e operações de banco com Kafka.

### 2. AOT Compilation & GraalVM Native Image
- **Spring Boot 4.x** oferece suporte nativo a **AOT (Ahead of Time) Compilation** via GraalVM.
- Compile a aplicação como Native Image para startup instantâneo e menor consumo de memória em containers.
- Configure o build Maven com o plugin `spring-boot-maven-plugin` e `native-maven-plugin`.
- Útil para deployments em serverless e edge computing.

### 3. Pattern Matching Avançado
- Aproveite **Sealed Classes** e **Records** combinados com pattern matching para validações de payload mais expressivas.
- Exemplo: Validar tipos de payload Kafka usando `instanceof` com padrões.

---

## 🏗️ Arquitetura e Clean Architecture

### 1. Separação de Conceitos (Separation of Concerns)
- **Domain**: Entidades puras do negócio. Sem referências a anotações do Spring, Hibernate/JPA ou bibliotecas de terceiros (Lombok é opcional, mas prefira construtores nativos e POJOs limpos).
- **Use Cases (Application)**: Contém a orquestração do negócio e as regras operacionais. Comunicam-se com o mundo externo apenas por interfaces (Ports).
- **Adapters**: Controllers, Consumers Kafka e Repositories. Convertem dados externos para o formato do Domínio.

### 2. Desacoplamento Reativo (Kafka)
- Isolar a thread HTTP da thread de processamento de mensagens.
- **Produtor**: O controller REST apenas valida o payload inicial e despacha para o Kafka de forma não-bloqueante utilizando um pool de threads assíncrono.
- **Consumidor**: O `@KafkaListener` opera de forma assíncrona, tratando erros e executando a lógica pesada (como o algoritmo de estabilização) em background.

---

## 🐳 Infraestrutura & Docker Compose

### 1. Stack de Desenvolvimento
- **PostgreSQL**: Banco de dados relacional principal, orquestrado via `docker-compose.yml`.
- **Kafka**: Message broker em modo KRaft (sem Zookeeper) para desacoplamento reativo entre produtor e consumidor.
- **Spring Boot Application**: Container com a aplicação Java 21, conectando-se aos serviços acima pela rede de bridge Docker.

### 2. Configuração de Conexão
- Variáveis de ambiente (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, etc.) são injetadas via `docker-compose` no arquivo `.env` ou no `docker-compose.yml`.
- Migrations de schema: **Flyway** na pasta `db/migration/` valida e versiona o PostgreSQL automaticamente no startup.

### 3. Boas Práticas de Container
- Use volumes nomeados para persistência de dados (PostgreSQL, Kafka).
- Configure health checks e `depends_on` para garantir inicialização na ordem correta.
- Exponha apenas as portas necessárias (5432 para PostgreSQL, 9092/29092 para Kafka, 8080 para API).

---

## 🧪 Estratégia de Testes e Qualidade

- **Testes Unitários**: Escreva testes com **JUnit 5** e **Mockito** focando em isolar a lógica de negócio dos Use Cases.
- **Testes de Integração**: Utilize **Testcontainers com PostgreSQL** para testar as transações e persistência de repositórios reais em um ambiente isolado e reproduzível.
- **Novo em Boot 4.x**: Spring Boot 4.x fornece suporte automático a **Testcontainers**, eliminando a necessidade de configuração manual.
  - Use `@ServiceConnection` para auto-conectar ao banco PostgreSQL testcontainer.
  - Use `@DynamicPropertySource` para injetar propriedades dinâmicas.
- **AssertJ**: Use a biblioteca AssertJ para asserções fluídas e legíveis (ex: `assertThat(resultado).isNotNull().hasSize(1);`).
- **Performance**: Use Virtual Threads em testes para paralelização eficiente.

---

## 📊 Observabilidade & Monitoring (Spring Boot 4.x)

- **Micrometer**: Integrado nativamente no Spring Boot 4.x. Configure exportadores de métricas (Prometheus, DataDog, etc).
- **OpenTelemetry**: Use para distributed tracing entre aplicação, Kafka e PostgreSQL.
- **Spring Boot Actuator**: Exponha endpoints de health, metrics, traces e threaddump em `/actuator/`.
- **Structured Logging**: Configure logs estruturados em JSON usando Spring Boot Logging com Logback/SLF4J para melhor observabilidade em produção.
- **Health Checks**: Configure health checks customizados para PostgreSQL, Kafka e componentes críticos do negócio usando `@Component implements HealthIndicator`.

---

## 🔐 Segurança & Spring Security 6.x (Spring Boot 4.x)

### 1. Autenticação e Autorização
- **JWT com Spring Security 6.x**: Use `JwtDecoder` e `JwtEncoder` nativos (sem JJWT manual se possível).
- **OAuth2 Resource Server**: Configure com suporte a Bearer Tokens via JWT ou OIDC.
- **Token-based para APIs**: Implemente autenticação stateless com `SecurityFilterChain` configurando `SessionCreationPolicy.STATELESS`.

### 2. Boas Práticas de Segurança
- **CSRF Protection**: Desabilite apenas para endpoints REST stateless (ex: `/api/**`), mantendo ativado para formulários HTML.
- **CORS Configuration**: Use `@CrossOrigin` ou configure globalmente em `WebSecurityConfigurerAdapter` para APIs públicas (Swagger UI, endpoints de pesagem).
- **Password Encoding**: Use `BCryptPasswordEncoder` com força mínima 12 (`new BCryptPasswordEncoder(12)`) para senhas de usuários administrativos.
- **Rate Limiting**: Configure com Spring Cloud Config ou `@RateLimiter` do Resilience4j para proteger endpoints críticos.

### 3. Configuração em application.yaml (Boot 4.x)
```yaml
server:
  servlet:
    session:
      cookie:
        secure: true
        http-only: true
        same-site: strict
  tomcat:
    threads:
      virtual:
        enabled: true
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${JWT_ISSUER_URI}
          jwk-set-uri: ${JWT_JWK_SET_URI}
```

---

## 🛠️ Configuração Maven & Build (Spring Boot 4.x)

- Use `spring-boot-maven-plugin` versão 4.x com suporte a Native Image.
- Configure `native-maven-plugin` para compilar como GraalVM Native Image (opcional, mas recomendado para produção).
- Use `maven-compiler-plugin` com `release=21` e `enablePreview=true` para suporte a features preview do Java 21.
- Configure `maven-surefire-plugin` para usar Virtual Threads em testes (via `JUnit5` e sistema de threading).

### Dependências Maven Essenciais

#### Spring Data JPA com Auditing

```xml
<!-- Spring Data JPA com suporte a Auditing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- PostgreSQL Driver -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Spring Data Commons (inclui @CreatedDate, @LastModifiedDate) -->
<!-- ⚠️ JÁ INCLUÍDO em spring-boot-starter-data-jpa, mas pode ser explicitado -->
<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-commons</artifactId>
</dependency>

<!-- Flyway para Migrations -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-flyway</artifactId>
</dependency>
```

#### Configuração Necessária em pom.xml

```xml
<build>
    <plugins>
        <!-- Spring Boot Maven Plugin 4.x -->
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <version>4.0.7</version>
        </plugin>
        
        <!-- Maven Compiler Plugin para Java 21 -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <release>21</release>
                <enablePreview>true</enablePreview>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Ativar @EnableJdbcAuditing na Configuração

```java
@Configuration
@EnableJdbcAuditing(auditorAwareRef = "auditorProvider")
public class AuditingConfig {
    
    /**
     * Bean que fornece o "quem" (criador/modificador) para @CreatedBy/@LastModifiedBy.
     * Retorna o usuário atual (SecurityContext) ou um valor padrão.
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            try {
                // Tenta obter o usuário autenticado
                var authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated()) {
                    return Optional.of(authentication.getName());
                }
            } catch (Exception e) {
                // Fallback se não houver autenticação
            }
            // Padrão: "SISTEMA" para processos automáticos
            return Optional.of("SISTEMA");
        };
    }
}

