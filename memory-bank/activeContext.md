# Active Context

## Foco Atual
Inicialização da estrutura base do projeto e modelagem de domínio.

## Decisões Recentes
- Arquitetura baseada em Clean Architecture com separação clara de camadas
- Uso de Kafka para desacoplamento entre recebimento de leituras e processamento
- PostgreSQL como banco principal com UUID nativo
- Java 21 com Virtual Threads para concorrência
- Flyway para versionamento de schema
- Autenticação de balanças via password no header

## Padrões Importantes
- **Records**: Usados para DTOs, payloads e eventos Kafka
- **Clean Architecture**: Domain isolado, Use Cases orquestram, Adapters convertem
- **Event-Driven**: Kafka como backbone para processamento assíncrono
- **Imutabilidade**: Preferência por objetos imutáveis e finais
- **UUID**: Chave primária em todas as entidades principais

## Próximos Passos
1. Criar estrutura de pastas do projeto (domain, usecases, adapters)
2. Implementar entidades JPA com mapeamento PostgreSQL
3. Criar migrations Flyway (schema + seed inicial)
4. Desenvolver algoritmo de estabilização de peso
5. Implementar API REST para recebimento de leituras
6. Configurar Kafka Producer/Consumer
7. Desenvolver use cases de negócio
8. Implementar camada de segurança JWT

## Considerações Ativas
- Threshold de estabilização: configurável (default 5kg em 2s)
- Margem de lucro: entre 5% e 20%, inversamente proporcional ao estoque
- Janela de leituras: últimos 5-10 segundos para análise
- Idempotência: baseada em balança + placa + transação ativa
- Particionamento Kafka: por balança para ordenação

