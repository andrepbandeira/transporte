# 🐳 Docker Compose Rules & Guidelines

Guia completo para configurar a stack de infraestrutura (PostgreSQL, Apache Kafka KRaft, Spring Boot 4.x) via Docker Compose.

---

## 📋 Índice

1. [Visão Geral](#visão-geral)
2. [Estrutura do docker-compose.yml](#estrutura-do-docker-composeyml)
3. [Apache Kafka em Modo KRaft](#apache-kafka-em-modo-kraft)
4. [PostgreSQL Configuração](#postgresql-configuração)
5. [Aplicação Spring Boot](#aplicação-spring-boot)
6. [Comandos Úteis](#comandos-úteis)
7. [Troubleshooting](#troubleshooting)

---

## 🎯 Visão Geral

A stack consiste em três serviços principais:

```
┌─────────────────────────────────────────────────────────┐
│                   Docker Network (Bridge)               │
│                    172.28.0.0/16                        │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  PostgreSQL  │  │    Kafka     │  │ Spring Boot  │  │
│  │   :5432      │  │   :9092/29092│  │   :8080      │  │
│  │              │  │   (KRaft)    │  │              │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│      Port:5432       Port:9092(ext)     Port:8080       │
│                      Port:29092(int)                    │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### Características:
- **PostgreSQL**: Banco de dados relacional com persistência via volumes
- **Kafka KRaft**: Modo nativo sem Zookeeper, dual listeners (PLAINTEXT + INTERNAL)
- **Spring Boot**: Aplicação Java 21 conectada via rede de bridge
- **Volumes Nomeados**: Persistência de dados entre restarts

---

## 🏗️ Estrutura do docker-compose.yml

### Versão Mínima Recomendada
```yaml
version: '3.9'  # Docker Compose 3.9+ para suporte a profiles e features avançadas
```

### Serviços Principais

#### 1. **PostgreSQL Service**

```yaml
services:
  postgres:
    image: postgres:16-alpine  # Imagem leve e oficial
    container_name: balancas-postgres
    environment:
      POSTGRES_DB: ${POSTGRES_DB:-transporte_db}
      POSTGRES_USER: ${POSTGRES_USER:-postgres}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-postgres123}
      # Configurações de Performance
      POSTGRES_INITDB_ARGS: "-c max_connections=200 -c shared_buffers=256MB"
    ports:
      - "5432:5432"  # Expõe apenas localmente (remover em produção)
    volumes:
      # Volume nomeado para persistência de dados
      - postgres-data:/var/lib/postgresql/data
      # Scripts de inicialização (opcional)
      - ./scripts/init-db.sql:/docker-entrypoint-initdb.d/01-init.sql
    networks:
      - balancas-net
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER:-postgres}"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 10s
```

#### 2. **Kafka KRaft Service**

```yaml
  kafka:
    image: apache/kafka:3.9.0  # Imagem oficial Apache Kafka com suporte KRaft
    container_name: balancas-kafka
    user: "root:root"  # Necessário para write no volume
    environment:
      # ============ CONFIGURAÇÃO KRAFT ============
      # Identificação do Nó no Cluster
      KAFKA_NODE_ID: "1"
      
      # Papéis do Nó (broker = produtor/consumidor, controller = quorum)
      KAFKA_PROCESS_ROLES: "broker,controller"
      
      # Quórum de Controladores (single-node: "1@kafka:9093")
      KAFKA_CONTROLLER_QUORUM_VOTERS: "1@kafka:9093"
      
      # ID do Cluster (gerar com: kafka-storage.sh random-uuid)
      KAFKA_CLUSTER_ID: "MkQkMzI3NTczNTAwODAwMDAw"
      
      # ============ LISTENERS (DUAL-STACK) ============
      # PLAINTEXT: externa (ESP32/localhost:9092)
      # INTERNAL: interna (containers via kafka:29092)
      # CONTROLLER: quórum KRaft (kafka:9093)
      KAFKA_LISTENERS: "PLAINTEXT://:9092,CONTROLLER://:9093,INTERNAL://:29092"
      
      # O que publicar para clientes de fora
      KAFKA_ADVERTISED_LISTENERS: "PLAINTEXT://localhost:9092,INTERNAL://kafka:29092"
      
      # Mapeamento de protocolos de segurança
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: "CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,INTERNAL:PLAINTEXT"
      
      # Listener que brokers usam entre si
      KAFKA_INTER_BROKER_LISTENER_NAME: "INTERNAL"
      
      # Listener do controlador
      KAFKA_CONTROLLER_LISTENER_NAMES: "CONTROLLER"
      
      # ============ TÓPICOS & ARMAZENAMENTO ============
      # Auto-criar tópicos no primeiro uso
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
      
      # Partições padrão para novos tópicos
      KAFKA_NUM_PARTITIONS: "3"
      
      # Diretório de logs KRaft
      KAFKA_LOG_DIRS: "/var/lib/kraft-combined-logs"
      
      # ============ REPLICAÇÃO (SINGLE-NODE) ============
      # Em cluster single-node, replication factor DEVE ser 1
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: "1"
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: "1"
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: "1"
      
      # ============ OTIMIZAÇÕES ============
      # Limpeza de logs (policy=delete mantém apenas N dias)
      KAFKA_LOG_RETENTION_HOURS: "168"
      KAFKA_LOG_RETENTION_BYTES: "1073741824"  # 1GB
      
      # Compressão de mensagens
      KAFKA_COMPRESSION_TYPE: "snappy"
    
    ports:
      - "9092:9092"    # PLAINTEXT (externo) - ESP32, localhost
      - "29092:29092"  # INTERNAL (interno) - containers
      # NÃO expor porta 9093 (CONTROLLER é apenas interno)
    
    volumes:
      - kafka-data:/var/lib/kraft-combined-logs
    
    networks:
      - balancas-net
    
    healthcheck:
      test: ["CMD", "kafka-broker-api-versions.sh", "--bootstrap-server=localhost:9092"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s
```

#### 3. **Spring Boot Application**

```yaml
  application:
    build:
      context: .
      dockerfile: Dockerfile
      args:
        # Argumentos de build
        JAVA_VERSION: "21"
    
    container_name: balancas-api
    
    depends_on:
      postgres:
        condition: service_healthy
      kafka:
        condition: service_healthy
    
    environment:
      # ============ SPRING BOOT CORE ============
      SPRING_PROFILES_ACTIVE: "docker"
      SPRING_APPLICATION_NAME: "transporte-api"
      
      # ============ DATASOURCE (PostgreSQL) ============
      SPRING_DATASOURCE_URL: "jdbc:postgresql://postgres:5432/${POSTGRES_DB:-transporte_db}"
      SPRING_DATASOURCE_USERNAME: ${POSTGRES_USER:-postgres}
      SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD:-postgres123}
      SPRING_DATASOURCE_DRIVER_CLASS_NAME: "org.postgresql.Driver"
      
      # ============ JPA & HIBERNATE ============
      SPRING_JPA_DATABASE_PLATFORM: "org.hibernate.dialect.PostgreSQL82Dialect"
      SPRING_JPA_HIBERNATE_DDL_AUTO: "validate"  # validate (migrations geridas por Flyway)
      SPRING_JPA_SHOW_SQL: "false"
      SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL: "true"
      SPRING_JPA_PROPERTIES_HIBERNATE_JDBC_BATCH_SIZE: "20"
      
      # ============ FLYWAY (Database Migrations) ============
      SPRING_FLYWAY_ENABLED: "true"
      SPRING_FLYWAY_BASELINE_ON_MIGRATE: "true"
      SPRING_FLYWAY_LOCATIONS: "classpath:db/migration"
      
      # ============ KAFKA BOOTSTRAP ============
      SPRING_KAFKA_BOOTSTRAP_SERVERS: "kafka:29092"  # Usa INTERNAL listener
      
      # Producer Configuration
      SPRING_KAFKA_PRODUCER_KEY_SERIALIZER: "org.apache.kafka.common.serialization.StringSerializer"
      SPRING_KAFKA_PRODUCER_VALUE_SERIALIZER: "org.springframework.kafka.support.serializer.JsonSerializer"
      SPRING_KAFKA_PRODUCER_ACKS: "all"  # Aguarda confirmação de todos os replicas
      SPRING_KAFKA_PRODUCER_RETRIES: "3"
      SPRING_KAFKA_PRODUCER_BATCH_SIZE: "16384"
      SPRING_KAFKA_PRODUCER_LINGER_MS: "10"
      
      # Consumer Configuration
      SPRING_KAFKA_CONSUMER_GROUP_ID: "balancas-consumer-group"
      SPRING_KAFKA_CONSUMER_AUTO_OFFSET_RESET: "earliest"
      SPRING_KAFKA_CONSUMER_KEY_DESERIALIZER: "org.apache.kafka.common.serialization.StringDeserializer"
      SPRING_KAFKA_CONSUMER_VALUE_DESERIALIZER: "org.springframework.kafka.support.serializer.JsonDeserializer"
      SPRING_KAFKA_CONSUMER_MAX_POLL_RECORDS: "500"
      SPRING_KAFKA_PROPERTIES_SPRING_JSON_TRUSTED_PACKAGES: "*"
      SPRING_KAFKA_PROPERTIES_SPRING_JSON_VALUE_DEFAULT_TYPE: "transporte.desafio.application.dto.PesagemEvent"
      
      # ============ APLICAÇÃO CUSTOMIZADA ============
      APP_BALANCE_TOKEN: ${BALANCE_TOKEN:-balanca-dev-token}
      APP_BALANCE_TOPIC: "pesagens"
      
      APP_STABILIZATION_WINDOW_READINGS: "5"
      APP_STABILIZATION_MAX_DELTA_KG: "5.0"
      APP_STABILIZATION_MIN_INTERVAL_MS: "2000"
      
      APP_MARGIN_MIN: "0.05"
      APP_MARGIN_MAX: "0.20"
      
      # ============ SEGURANÇA & JWT ============
      APP_SECURITY_JWT_SECRET: ${JWT_SECRET:-chave-desenvolvimento-transporte-2026}
      APP_SECURITY_JWT_EXPIRATION_MS: "28800000"  # 8 horas
      
      # ============ JVM TUNING ============
      JAVA_OPTS: "-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
      JAVA_TOOL_OPTIONS: "-Dfile.encoding=UTF-8"
    
    ports:
      - "8080:8080"  # API REST
    
    volumes:
      # Volume para logs (persiste entre restarts)
      - ./logs:/app/logs
      # Volume para dados (se usar file-based H2, por exemplo)
      - ./data:/app/data
    
    networks:
      - balancas-net
    
    restart: unless-stopped
    
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
```

---

## 🚀 Apache Kafka em Modo KRaft

### O que é KRaft?

**KRaft (Kafka Raft)** é o novo modo de operação do Kafka que elimina a dependência de Zookeeper. Usa o algoritmo **Raft** para consenso distribuído.

### Vantagens:
- ✅ **Simplicidade**: Um único processo gerencia broker e controlador
- ✅ **Performance**: Menor latência em eleições
- ✅ **Escalabilidade**: Facilita multi-nó em produção
- ✅ **Single Node**: Perfeito para desenvolvimento com `KAFKA_PROCESS_ROLES: broker,controller`

### Listeners Explicados

```
┌─────────────────────────────────────────────────────┐
│                   KAFKA LISTENERS                   │
├─────────────────────────────────────────────────────┤
│                                                     │
│  PLAINTEXT (9092)                                   │
│  └─ Usado por: ESP32, clientes localhost            │
│  └─ ADVERTISED: localhost:9092                      │
│  └─ Acessível de: FORA do Docker                    │
│                                                     │
│  INTERNAL (29092)                                   │
│  └─ Usado por: Containers internos (app)            │
│  └─ ADVERTISED: kafka:29092 (service name)          │
│  └─ Acessível de: DENTRO do Docker (bridge)        │
│                                                     │
│  CONTROLLER (9093)                                  │
│  └─ Usado por: Nós do cluster KRaft                 │
│  └─ NÃO ADVERTISED externamente                     │
│  └─ Acessível de: Apenas nós do cluster             │
│                                                     │
└─────────────────────────────────────────────────────┘
```

### Configuração Mínima (Single Node)

```yaml
environment:
  KAFKA_NODE_ID: "1"
  KAFKA_PROCESS_ROLES: "broker,controller"
  KAFKA_CONTROLLER_QUORUM_VOTERS: "1@kafka:9093"
  KAFKA_CLUSTER_ID: "MkQkMzI3NTczNTAwODAwMDAw"
  KAFKA_LISTENERS: "PLAINTEXT://:9092,CONTROLLER://:9093,INTERNAL://:29092"
  KAFKA_ADVERTISED_LISTENERS: "PLAINTEXT://localhost:9092,INTERNAL://kafka:29092"
  KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: "CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,INTERNAL:PLAINTEXT"
  KAFKA_INTER_BROKER_LISTENER_NAME: "INTERNAL"
  KAFKA_CONTROLLER_LISTENER_NAMES: "CONTROLLER"
```

### Gerar novo CLUSTER_ID

```bash
# Se precisar de um novo cluster ID único:
docker run --rm apache/kafka:3.9.0 kafka-storage.sh random-uuid
# Output: exemplo-de-uuid-aleatorio
```

### Criação Automática de Tópicos

```yaml
environment:
  KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
  KAFKA_NUM_PARTITIONS: "3"
```

Alternativamente, criar manualmente via script:

```bash
docker exec balancas-kafka kafka-topics.sh \
  --create \
  --bootstrap-server localhost:9092 \
  --topic pesagens \
  --partitions 3 \
  --replication-factor 1
```

---

## 🐘 PostgreSQL Configuração

### Variáveis de Ambiente Essenciais

```yaml
environment:
  POSTGRES_DB: transporte_db        # Database a criar
  POSTGRES_USER: postgres            # Usuário admin
  POSTGRES_PASSWORD: postgres123     # Senha (MUDAR EM PRODUÇÃO!)
  POSTGRES_INITDB_ARGS: |
    -c max_connections=200
    -c shared_buffers=256MB
    -c effective_cache_size=1GB
    -c maintenance_work_mem=64MB
    -c checkpoint_completion_target=0.9
    -c wal_buffers=16MB
    -c work_mem=4MB
```

### Volumes e Persistência

```yaml
volumes:
  # Volume nomeado - persiste entre docker-compose down/up
  - postgres-data:/var/lib/postgresql/data
  
  # Scripts de inicialização (executam automaticamente)
  - ./scripts/init-db.sql:/docker-entrypoint-initdb.d/01-init.sql
```

### Health Check

```yaml
healthcheck:
  test: ["CMD-SHELL", "pg_isready -U postgres"]
  interval: 10s
  timeout: 5s
  retries: 5
  start_period: 10s
```

### Conectar ao PostgreSQL Local

```bash
# Via psql
psql -h localhost -U postgres -d transporte_db

# Via Docker
docker exec -it balancas-postgres psql -U postgres -d transporte_db

# Verificar status
docker exec balancas-postgres pg_isready
```

---

## 📦 Aplicação Spring Boot

### Conexão com Kafka (application.yml)

```yaml
spring:
  kafka:
    bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:kafka:29092}
    
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all
      retries: 3
      
    consumer:
      group-id: balancas-consumer-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"
        spring.json.value.default.type: transporte.desafio.application.dto.PesagemEvent
```

### Conexão com PostgreSQL (application.yml)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${POSTGRES_HOST:localhost}:${POSTGRES_PORT:5432}/${POSTGRES_DB:transporte_db}
    username: ${POSTGRES_USER:postgres}
    password: ${POSTGRES_PASSWORD:postgres123}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 20000
      
  jpa:
    hibernate:
      ddl-auto: validate
    database-platform: org.hibernate.dialect.PostgreSQL82Dialect
    
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
```

### Exemplo: Controller Enviando para Kafka

```java
@RestController
@RequestMapping("/api/v1/pesagens")
public class PesagemController {
    
    private final KafkaTemplate<String, PesagemEvent> kafkaTemplate;
    
    public PesagemController(KafkaTemplate<String, PesagemEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    @PostMapping
    public ResponseEntity<String> receberPesagem(@Valid @RequestBody PesagemPayload payload) {
        // Cria evento
        PesagemEvent event = new PesagemEvent(
            payload.balancaId(),
            payload.placa(),
            payload.peso(),
            LocalDateTime.now()
        );
        
        // Envia para Kafka (non-blocking)
        kafkaTemplate.send("pesagens", payload.balancaId(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Erro ao enviar para Kafka", ex);
                } else {
                    log.info("Pesagem enviada: topic={}, partition={}, offset={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                }
            });
        
        // Retorna imediatamente (202 Accepted)
        return ResponseEntity.accepted().build();
    }
}
```

### Exemplo: Consumer Recebendo do Kafka e Salvando no PostgreSQL

```java
@Component
public class PesagemConsumer {
    
    private final ProcessarPesagemUseCase processarPesagemUseCase;
    
    public PesagemConsumer(ProcessarPesagemUseCase processarPesagemUseCase) {
        this.processarPesagemUseCase = processarPesagemUseCase;
    }
    
    @KafkaListener(
        topics = "pesagens",
        groupId = "balancas-consumer-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumirPesagem(PesagemEvent event, Acknowledgment ack) {
        try {
            log.info("Consumindo pesagem: balança={}, placa={}, peso={}",
                event.balancaId(), event.placa(), event.peso());
            
            // Processar (validar, estabilizar, persistir)
            processarPesagemUseCase.processar(event);
            
            // Confirmar offset apenas após sucesso
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Erro ao processar pesagem", e);
            // Retentar (Spring Kafka faz retry automático)
            throw new RuntimeException(e);
        }
    }
}
```

---

## 🔧 Comandos Úteis

### Iniciar Stack

```bash
# Build e inicia todos os serviços
docker-compose up -d --build

# Apenas inicia (sem rebuild)
docker-compose up -d

# Inicia com logs visíveis
docker-compose up
```

### Verificar Status

```bash
# Status de todos os containers
docker-compose ps

# Logs específicos
docker-compose logs postgres -f
docker-compose logs kafka -f
docker-compose logs application -f

# Todos os logs
docker-compose logs -f
```

### Kafka: Listar Tópicos

```bash
docker exec balancas-kafka kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --list
```

### Kafka: Consumir Mensagens (Debug)

```bash
docker exec -it balancas-kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic pesagens \
  --from-beginning
```

### Kafka: Produzir Mensagens (Teste)

```bash
docker exec -it balancas-kafka kafka-console-producer.sh \
  --bootstrap-server localhost:9092 \
  --topic pesagens \
  --property parse.key=true \
  --property key.separator=:
```

### PostgreSQL: Executar SQL

```bash
docker exec -it balancas-postgres psql -U postgres -c "SELECT version();"

# Backup
docker exec balancas-postgres pg_dump -U postgres transporte_db > backup.sql

# Restore
docker exec -i balancas-postgres psql -U postgres transporte_db < backup.sql
```

### Parar Stack

```bash
# Parar (containers ainda existem)
docker-compose stop

# Parar e remover (containers + volumes)
docker-compose down

# Parar e remover TUDO (incluindo volumes nomeados)
docker-compose down -v
```

---

## 🛠️ Troubleshooting

### Kafka não inicia: "Cannot create directory"

**Problema**: Permissões de volume
```bash
# Solução
docker-compose down -v
sudo chown 1000:1000 $(pwd)  # ou usar user: "root:root" no service
docker-compose up -d
```

### Aplicação não conecta ao Kafka: "Connection refused"

**Problema**: Usando host.docker.internal ou localhost:9092 internamente
```yaml
# ❌ ERRADO
SPRING_KAFKA_BOOTSTRAP_SERVERS: "localhost:9092"

# ✅ CORRETO
SPRING_KAFKA_BOOTSTRAP_SERVERS: "kafka:29092"  # Service name + INTERNAL listener
```

### PostgreSQL: "could not connect"

**Problema**: Aguardar health check
```yaml
# Na aplicação, adicionar depends_on com condition
depends_on:
  postgres:
    condition: service_healthy  # Aguarda health check passar
```

### Kafka: "Broker may not be available"

**Problema**: Replication factor > 1 em single node
```yaml
# ❌ ERRADO
KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: "3"

# ✅ CORRETO (single node)
KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: "1"
KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: "1"
```

### Migração Flyway falha

**Problema**: DDL-AUTO não é "validate"
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Não criar tabelas, apenas validar
  flyway:
    enabled: true        # Flyway responsável por criar/migrar
```

### Tópico não criado automaticamente

**Problema**: AUTO_CREATE_TOPICS_ENABLE=false ou erro de replication
```bash
# Criar manualmente
docker exec balancas-kafka kafka-topics.sh \
  --create \
  --bootstrap-server localhost:9092 \
  --topic pesagens \
  --partitions 3 \
  --replication-factor 1 \
  --if-not-exists
```

---

## 📊 Monitoramento & Health Checks

### Verificar Saúde da Stack

```bash
# Health checks integrados
curl http://localhost:8080/actuator/health

# Metricas
curl http://localhost:8080/actuator/metrics

# Informações do Kafka
curl http://localhost:8080/actuator/health/kafka
```

### Docker Compose Health Status

```bash
# Listar com health
docker-compose ps

# Exemplo output:
# NAME                  STATUS
# balancas-postgres     healthy
# balancas-kafka        healthy
# balancas-api          healthy
```

---

## 🚀 Exemplo Completo: .env

```bash
# .env (nunca commitar em git!)
POSTGRES_DB=transporte_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres123

BALANCE_TOKEN=balanca-dev-token
JWT_SECRET=chave-super-secreta-de-32-caracteres-minimo!!!

SPRING_PROFILES_ACTIVE=docker
```

Usar em docker-compose.yml:
```yaml
environment:
  POSTGRES_DB: ${POSTGRES_DB}
  POSTGRES_USER: ${POSTGRES_USER}
  POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
```

---

## ✅ Checklist Final

- [ ] Docker e Docker Compose instalados (`docker -v`, `docker-compose -v`)
- [ ] Arquivo `docker-compose.yml` na raiz do projeto
- [ ] Arquivo `.env` criado com credenciais seguras
- [ ] `.env` adicionado ao `.gitignore`
- [ ] Dockerfile na raiz do projeto
- [ ] Scripts SQL em `./scripts/init-db.sql` (se houver)
- [ ] Migrations Flyway em `src/main/resources/db/migration/`
- [ ] Application YAML configurado com variáveis de ambiente
- [ ] Ports (5432, 9092, 8080) disponíveis localmente

### Iniciar

```bash
docker-compose up -d --build
docker-compose logs -f application
```

### Validar

```bash
curl http://localhost:8080/swagger-ui.html
curl http://localhost:8080/actuator/health

docker exec balancas-postgres psql -U postgres -c "SELECT COUNT(*) FROM pg_tables;"
docker exec balancas-kafka kafka-topics.sh --bootstrap-server localhost:9092 --list
```

---

**Autor**: Sistema de Ingestão e Balanças de Grãos  
**Versão**: 1.0  
**Data**: 2026-08-10  
**Stack**: PostgreSQL 16 + Kafka 3.9.0 KRaft + Spring Boot 4.x + Java 21
