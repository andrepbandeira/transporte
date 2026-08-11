# Progress

## O que funciona
- ✅ Estrutura inicial do projeto criada
- ✅ Documentação de requisitos completa (entidades, fluxo, padrões)
- ✅ Docker Compose configurado (PostgreSQL, Kafka, App)
- ✅ Memory Bank inicializado

## O que está em desenvolvimento
- 🔄 Modelagem de domínio (entidades JPA)
- 🔄 Estrutura de pastas Clean Architecture
- 🔄 Migrations Flyway (schema + seed)

## O que falta construir

### Fase 1: Estrutura Base (Próxima)
- [ ] Criar estrutura de pacotes (domain, application, adapters)
- [ ] Implementar entidades JPA (Filial, TipoGrao, Caminhao, Balanca, TransacaoTransporte, Doca, Pesagem)
- [ ] Criar migrations Flyway V1 (schema completo)
- [ ] Criar migration V2 (seed inicial)
- [ ] Configurar Auditoria JPA (@EnableJdbcAuditing)

### Fase 2: Core de Pesagem
- [ ] Implementar algoritmo de estabilização
- [ ] Criar DTOs e Records (LeituraBalança, PesagemEvent, etc.)
- [ ] Desenvolver Kafka Producer (envio de leituras)
- [ ] Desenvolver Kafka Consumer (processamento)
- [ ] Implementar Use Cases:
  - [ ] ProcessarLeituraBalançaUseCase
  - [ ] EstabilizarPesagemUseCase
  - [ ] PersistirPesagemUseCase
  - [ ] AtualizarDocaUseCase
- [ ] Criar Repositories (JPA + Ports)

### Fase 3: API REST
- [ ] Controller: POST /api/v1/balanca/leitura
- [ ] Controller: POST /api/v1/transacoes
- [ ] Controller: GET /api/v1/transacoes/{id}
- [ ] Controller: GET /api/v1/doca/relatorio
- [ ] Segurança: JWT para endpoints administrativos
- [ ] Autenticação de balanças (password no header)

### Fase 4: Regras de Negócio
- [ ] Cálculo de custo da carga
- [ ] Cálculo de margem de lucro (5% a 20%)
- [ ] Cálculo de preço de venda
- [ ] Atualização automática de estoque na doca
- [ ] Validação de transações ativas (idempotência)

### Fase 5: Testes e Qualidade
- [ ] Testes unitários dos Use Cases
- [ ] Testes de integração com Testcontainers
- [ ] Testes do algoritmo de estabilização
- [ ] Testes de integração Kafka

### Fase 6: Observabilidade
- [ ] Configurar Actuator endpoints
- [ ] Métricas de pesagem (Micrometer)
- [ ] Logging estruturado (JSON)
- [ ] Distributed tracing (OpenTelemetry)

## Status Atual
**Fase:** Inicialização  
**Próximo Marco:** Modelagem de domínio e estrutura de pastas  
**Bloqueios:** Nenhum no momento

## Decisões de Design Importantes
- ✅ Clean Architecture para separação de concerns
- ✅ Kafka para processamento assíncrono (não bloquear API)
- ✅ UUID nativo PostgreSQL para chaves primárias
- ✅ Virtual Threads para concorrência eficiente
- ✅ Flyway para versionamento de schema
- ✅ @EnableJdbcAuditing para auditoria automática

## Lições Aprendidas
- Nenhuma lição registrada ainda (projeto em fase inicial)

## Issues Conhecidos
- Nenhum issue conhecido no momento

