# System Patterns

## Arquitetura
Clean Architecture (Ports e Adapters) com separação estrita de camadas:
- **Domain**: Entidades puras sem dependências externas
- **Application**: Use Cases e regras de negócio (Ports)
- **Adapters**: Controllers REST, Consumers Kafka, Repositories JPA

## Padrões de Design

### 1. Repository Pattern
- Interfaces no domain (`PesagemRepositoryPort`)
- Implementações em adapters (`PesagemJpaRepository`)
- Isolamento de detalhes de persistência

### 2. Use Case Pattern
- Um Use Case por operação de negócio
- Injeção via construtor (Spring Boot 4.x)
- Retorno de DTOs/Records, não entidades JPA

### 3. Event-Driven com Kafka
- **Producer**: Controller REST envia eventos não-bloqueantes
- **Consumer**: Processa leituras e detecta estabilização
- **DLQ**: Dead Letter Queue para mensagens com erro

### 4. Mapper/Adapter Pattern
- Conversão entre Domain Model e JPA Entities
- Conversão entre DTOs e Domain Model
- Isolamento de formatos externos

## Fluxos Principais

### Fluxo de Leitura de Balança
1. ESP32 → API REST (POST /api/v1/balanca/leitura)
2. Controller valida autenticação (token/password)
3. Envia `LeituraBalançaEvent` para Kafka tópico `pesagens`
4. Kafka Consumer processa assincronamente
5. Algoritmo de estabilização analisa janela de leituras
6. Quando estabilizado: persiste Pesagem + atualiza Doca
7. Acknowledge apenas após sucesso (manual ack)

### Fluxo de Transação
1. Criação: `POST /api/v1/transacoes`
2. Associa caminhão, filial, tipo_grão, balança
3. Status: `EM_ANDAMENTO`
4. Durante pesagens: associa leituras à transação
5. Finalização: transação marcada como `FINALIZADA`

## Estratégias

### Estabilização de Peso
- Janela deslizante de últimas N leituras
- Critério: variação máxima < threshold por T segundos
- Configurável: `window_size`, `max_delta_kg`, `min_interval_ms`
- Descarta leituras após estabilização

### Idempotência
- Chave composta: balanca_id + placa + transacao_id ativa
- Verifica se transação já finalizada antes de processar
- Ignora leituras de pesagem já estabilizada

### Cálculo de Margem
- Preço venda = preço compra * (1 + margem)
- Margem mínima: 5%, máxima: 20%
- Margem ajustada inversamente por estoque disponível
- Quanto menor o estoque, maior a margem

## Tratamento de Erros
- **Kafka**: Retry com backoff + DLQ após N tentativas
- **Validação**: Exceções específicas (`BalancaNaoAutorizadaException`)
- **Negócio**: Regras validadas em Use Cases
- **Persistência**: Rollback transacional em caso de falha

## Auditoria
- `@EnableJdbcAuditing` para campos automáticos
- `@CreatedDate`, `@LastModifiedDate`
- `@CreatedBy`, `@LastModifiedBy` (opcional)
- Auditor padrão: "SISTEMA" para processos automáticos

