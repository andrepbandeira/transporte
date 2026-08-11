# Tech Context

## Stack Tecnológico

### Backend
- **Java 21** com Virtual Threads (Project Loom)
- **Spring Boot 4.x** com suporte nativo a Virtual Threads
- **Spring Data JPA** para persistência
- **Spring Kafka** para mensageria
- **Spring Security 6.x** com JWT
- **Flyway** para versionamento de banco
- **Micrometer + OpenTelemetry** para observabilidade

### Banco de Dados
- **PostgreSQL 16** com tipo UUID nativo
- **Flyway** migrations versionadas
- **HikariCP** connection pool

### Mensageria
- **Apache Kafka 3.9.0** em modo KRaft (sem Zookeeper)
- Dual listeners: PLAINTEXT (externo) + INTERNAL (interno)
- Tópico: `pesagens` com 3 partições
- JsonSerializer/Deserializer para eventos

### Infraestrutura
- **Docker Compose** para orquestração local
- **Dockerfile** multi-stage para produção
- Volumes nomeados para PostgreSQL e Kafka
- Health checks para todos os serviços

## Dependências Principais (Maven)
```xml
- spring-boot-starter-web (REST API)
- spring-boot-starter-data-jpa (Persistência)
- spring-boot-starter-kafka (Mensageria)
- spring-boot-starter-security (Segurança)
- spring-boot-starter-flyway (Migrations)
- spring-boot-starter-actuator (Health/Metrics)
- postgresql (Driver)
- org.springdoc:springdoc-openapi (Swagger)
```

## Configurações Chave

### Application YAML
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/transporte_db
    username: postgres
    password: postgres123
  
  jpa:
    hibernate:
      ddl-auto: validate
    database-platform: org.hibernate.dialect.PostgreSQL82Dialect
  
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: StringSerializer
      value-serializer: JsonSerializer
    consumer:
      group-id: balancas-consumer-group
      auto-offset-reset: earliest
  
  flyway:
    enabled: true
    locations: classpath:db/migration

server:
  tomcat:
    threads:
      virtual:
        enabled: true
```

### Docker Compose
- PostgreSQL: porta 5432
- Kafka: porta 9092 (PLAINTEXT) + 29092 (INTERNAL)
- Aplicação: porta 8080
- Rede bridge: 172.28.0.0/16

## Padrões de Código

### Records para DTOs
```java
public record PesagemRequest(
    String balancaId,
    String password,
    String placa,
    BigDecimal peso
) {}
```

### UUID como PK
```java
@Id
@Column(name = "id", nullable = false, updatable = false)
private UUID id;

@PrePersist
public void prePersist() {
    if (this.id == null) {
        this.id = UUID.randomUUID();
    }
    this.createdAt = LocalDateTime.now();
}
```

### Injeção via Construtor
```java
@Service
public class ProcessarPesagemUseCase {
    private final PesagemRepositoryPort repository;
    
    public ProcessarPesagemUseCase(PesagemRepositoryPort repository) {
        this.repository = repository;
    }
}
```

## Ferramentas de Desenvolvimento
- **IDE**: VS Code / IntelliJ IDEA
- **Build**: Maven 3.9+
- **Testes**: JUnit 5 + Mockito + Testcontainers
- **Qualidade**: AssertJ para asserções
- **Versionamento**: Git

## Variáveis de Ambiente
- `POSTGRES_DB`: nome do banco
- `POSTGRES_USER`: usuário PostgreSQL
- `POSTGRES_PASSWORD`: senha PostgreSQL
- `BALANCE_TOKEN`: token para autenticação de balanças
- `JWT_SECRET`: chave secreta para JWT

