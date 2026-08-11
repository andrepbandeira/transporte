# Product Context

## Por que este projeto existe?
O sistema resolve a necessidade de automatizar e controlar o processo de pesagem de grãos em unidades operacionais (filiais), eliminando processos manuais, reduzindo erros e proporcionando rastreabilidade completa das operações de transporte.

## Problemas que resolve
- **Controle manual de pesagens**: Elimina registro manual de pesos e cálculos
- **Falta de rastreabilidade**: Registra todo o ciclo de transporte com timestamps
- **Inconsistência de estoque**: Mantém saldo atualizado em tempo real na doca
- **Cálculos manuais de custo/margem**: Automatiza cálculos financeiros
- **Duplicidade de registros**: Garante idempotência no processamento de leituras
- **Integração entre sistemas**: Conecta balanças físicas com sistema de gestão via Kafka

## Como deve funcionar
1. Balanças enviam leituras contínuas (100ms) para API
2. API valida autenticação e despacha eventos para Kafka
3. Consumer processa leituras e detecta estabilização
4. Quando estabilizado, persiste pesagem e atualiza doca
5. Sistema calcula custo, margem e disponibilidade
6. Administradores consultam relatórios via API

## Experiência do usuário
- **Balanças**: Envio transparente de leituras sem intervenção manual
- **Operadores**: Interface simples para cadastros e consultas
- **Administradores**: Relatórios completos de custo, margem e estoque
- **Sistema**: Processamento automático e assíncrono, sem bloqueios

## Valor para o negócio
- Redução de erros humanos no registro de pesagens
- Controle preciso de estoque em tempo real
- Rastreabilidade completa para auditoria
- Cálculos automáticos de custo e margem
- Escalabilidade para múltiplas filiais e balanças
- Integração nativa com infraestrutura existente

