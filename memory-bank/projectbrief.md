# Project Brief

## Nome do Projeto
Sistema de Pesagem de Grãos - Transporte API

## Visão Geral
Sistema de ingestão e controle de pesagem de grãos com arquitetura baseada em eventos, utilizando Spring Boot 4.x, Java 21, PostgreSQL e Apache Kafka em modo KRaft.

## Objetivos Principais
- Receber leituras contínuas de balanças físicas (a cada 100ms)
- Implementar algoritmo de estabilização de peso
- Persistir apenas pesagens estabilizadas
- Controlar estoque de grãos na doca
- Calcular margem de lucro e preço de venda
- Gerenciar transações de transporte completas
- Garantir idempotência e prevenção de duplicidade

## Escopo Inicial
- Cadastro de filiais, tipos de grão, caminhões, balanças e docas
- Criação e gerenciamento de transações de transporte
- Processamento de leituras de balança via API REST
- Consumo de eventos Kafka para processamento assíncrono
- Cálculo de custo, margem e preço de venda
- Gestão de saldo disponível na doca

## Entidades Principais
1. **Filial** - Unidades operacionais
2. **TipoGrao** - Tipos de grão com preço de compra
3. **Caminhao** - Caminhões cadastrados com tara
4. **Balanca** - Balanças físicas com autenticação
5. **TransacaoTransporte** - Ciclo de transporte completo
6. **Doca** - Controle de estoque por tipo de grão
7. **Pesagem** - Pesagens estabilizadas consolidadas

## Requisitos Técnicos
- Java 21 com Virtual Threads
- Spring Boot 4.x com Clean Architecture
- PostgreSQL 16 com UUID nativo
- Apache Kafka 3.9.0 (modo KRaft)
- Flyway para migrations
- JWT para autenticação
- Docker Compose para orquestração

## Status
Em desenvolvimento inicial - Fase de modelagem e estrutura base

# Project Brief

## Nome do Projeto
Sistema de Pesagem de Gr�os - Transporte API

## Vis�o Geral
Sistema de ingest�o e controle de pesagem de gr�os com arquitetura baseada em eventos, utilizando Spring Boot 4.x, Java 21, PostgreSQL e Apache Kafka em modo KRaft.

## Objetivos Principais
- Receber leituras cont�nuas de balan�as f�sicas (a cada 100ms)
- Implementar algoritmo de estabiliza��o de peso
- Persistir apenas pesagens estabilizadas
- Controlar estoque de gr�os na doca
- Calcular margem de lucro e pre�o de venda
- Gerenciar transa��es de transporte completas
- Garantir idempot�ncia e preven��o de duplicidade

## Escopo Inicial
- Cadastro de filiais, tipos de gr�o, caminh�es, balan�as e docas
- Cria��o e gerenciamento de transa��es de transporte
- Processamento de leituras de balan�a via API REST
- Consumo de eventos Kafka para processamento ass�ncrono
- C�lculo de custo, margem e pre�o de venda
- Gest�o de saldo dispon�vel na doca

## Entidades Principais
1. **Filial** - Unidades operacionais
2. **TipoGrao** - Tipos de gr�o com pre�o de compra
3. **Caminhao** - Caminh�es cadastrados com tara
4. **Balanca** - Balan�as f�sicas com autentica��o
5. **TransacaoTransporte** - Ciclo de transporte completo
6. **Doca** - Controle de estoque por tipo de gr�o
7. **Pesagem** - Pesagens estabilizadas consolidadas

## Requisitos T�cnicos
- Java 21 com Virtual Threads
- Spring Boot 4.x com Clean Architecture
- PostgreSQL 16 com UUID nativo
- Apache Kafka 3.9.0 (modo KRaft)
- Flyway para migrations
- JWT para autentica��o
- Docker Compose para orquestra��o

## Status
Em desenvolvimento inicial - Fase de modelagem e estrutura base
