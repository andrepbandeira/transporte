# Sistema de Pesagem de Grãos - Transporte API

Sistema de ingestão e controle de pesagem de grãos com arquitetura baseada em eventos.

## 🏛️ Arquitetura

Clean Architecture (Ports e Adapters):
- **Domain**: Entidades puras, enums, exceções e serviços de domínio
- **Application**: Use Cases, DTOs, ports (in/out)
- **Adapters**: Controllers REST, Consumers Kafka, Repositories JPA

## 🚀 Tecnologias

- **Java 21** com Virtual Threads
- **Spring Boot 4.x** com Clean Architecture
- **PostgreSQL 16** com UUID nativo
- **Apache Kafka 3.9.0** (modo KRaft)
- **Flyway** para migrations
- **JWT** para autenticação (endpoints administrativos / relatórios)
- **Micrometer + OpenTelemetry** para observabilidade

## 🤖 Documentação Adicional

- [uso de IA no projeto](readme2.md)

## 📦 Pré-requisitos
- Java 21+
- Maven 3.9+
- Docker e Docker Compose

## ⚙️ Configuração

1. Clone o repositório
2. Copie o arquivo `.env.example` para `.env`
3. Ajuste as variáveis conforme necessário

```bash
cp .env.example .env
```

## 🐳 Executando com Docker Compose

```bash
# Subir todos os serviços (PostgreSQL, Kafka, App)
docker-compose up -d --build

# Ver logs
docker-compose logs -f application

# Parar serviços
docker-compose down
```

## 💻 Executando Localmente

1. Suba PostgreSQL e Kafka:
```bash
docker-compose up -d postgres kafka
```

2. Execute a aplicação:
```bash
mvn spring-boot:run
```

3. Acesse:
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Actuator: http://localhost:8080/actuator

## 🧪 Testes

```bash
# Todos os testes
mvn test

# Testes específicos
mvn test -Dtest=AlgoritmoEstabilizacaoTest
```

## 📌 Endpoints Principais

### Balanças (Ingestão IoT)
- `POST /api/v1/balanca/leitura` - Receber leituras de peso (Autenticado via token da balança)

### Transações e Operações
- `POST /api/v1/transacoes` - Criar transação de transporte
- `GET /api/v1/transacoes/{id}` - Buscar transação

### Relatórios e Estoque
- `GET /api/v1/doca/relatorio` - Relatório de estoque e margem (Protegido por JWT)

## 📂 Estrutura do Projeto

```
src/main/java/transporte/desafio/
+-- adapter/
|   +-- inbound/
|   |   +-- kafka/          # Kafka Consumers
|   |   +-- rest/           # Controllers REST
|   +-- outbound/
|       +-- kafka/          # Kafka Producer
|       +-- persistence/    # JPA Entities e Repositories
+-- application/
|   +-- dto/                # Data Transfer Objects
|   +-- ports/              # Interfaces (in/out)
|   +-- service/            # Use Cases
+-- config/                 # Configurações Spring
+-- domain/
|   +-- enums/              # Enums do domínio
|   +-- exception/          # Exceções customizadas
|   +-- model/              # Entidades do domínio
|   +-- service/            # Serviços de domínio
+-- common/                 # Utilitários e exceções globais
+-- tools/                  # Ferramentas auxiliares
```

## 🔐 Autenticação e Segurança

### 1. Balanças (ESP32 / IoT)
- Autenticação por token/senha da balança validada por requisição (ou header de credencial da balança cadastrada).
- O token da balança padrão pode ser configurado via `BALANCE_TOKEN`.

### 2. Endpoints Administrativos e Relatórios
- Protegidos por **JWT Bearer Token** (OAuth2 Resource Server).
- O sistema valida tokens JWT assinados com a chave secreta configurada (`APP_SECURITY_JWT_SECRET` / `JWT_SECRET`).
- *Nota*: A tabela e o cadastro de usuários (`Usuario`) foram removidos do escopo da aplicação; a autenticação JWT valida o token gerado para acesso aos endpoints protegidos.

### 🔑 Gerar Token JWT (Exemplo prático via jwt.io)
Para acessar endpoints administrativos protegidos por JWT, você pode gerar um token de teste rapidamente no site [jwt.io](https://jwt.io/):

1. Acesse **[jwt.io](https://jwt.io/)**.
2. No painel esquerdo (**HEADER**), insira:
   ```json
   {
     "alg": "HS256",
     "typ": "JWT"
   }
   ```
3. No painel do meio (**PAYLOAD**), insira:
   ```json
   {
     "sub": "admin",
     "iat": 1718000000,
     "exp": 1800000000
   }
   ```
4. Na parte inferior do painel esquerdo, na seção de assinatura (**Verify Signature**):
   * Desmarque a caixa **"secret base64 encoded"** (se estiver marcada).
   * Insira a chave secreta padrão: `chave-desenvolvimento-transporte-2026` *(ou a sua `APP_SECURITY_JWT_SECRET` configurada no `.env` / `docker-compose.yml`)*.
5. Copie o token gerado no painel direito (**Encoded**) e envie nas requisições administrativas:
   ```http
   Authorization: Bearer <SEU_TOKEN_JWT>
   ```


## 🗄️ Migrations

As migrations Flyway estão em `src/main/resources/db/migration/`:
- `V1__create_tables.sql` - Criação do schema (Filial, TipoGrao, Caminhao, Balanca, TransacaoTransporte, Doca, Pesagem)

## 📊 Monitoramento

- **Health Checks**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Prometheus**: `/actuator/prometheus`
- **Kafka**: `/actuator/kafka`

## 📄 Licença

Projeto privado - Sistema de Pesagem de Grãos


