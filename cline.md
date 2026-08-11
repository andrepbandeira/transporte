# 🌾 Regras do Projeto: Sistema de Ingestão e Balanças de Grãos

## 🎯 Perfil e Persona
- Atue como Arquiteto de Software Sênior especialista em Java, Spring Boot e Clean Architecture.
- Foco em alta performance, concorrência, idempotência e desacoplamento de código.


## 🛠️ Stack Tecnológica
- **Linguagem**: Java 21 com recursos modernos (Records, Pattern Matching, Virtual Threads).
- **Framework**: Spring Boot 4.x (Web, Data JPA, Validation, Virtual Threads suportado nativamente).
- **Banco de Dados**: PostgreSQL (Orquestrado via Docker Compose para Desenvolvimento e Produção).
- **Gerenciador de Dependências**: Maven.
- **Mensageria**: Apache Kafka em modo KRaft (sem Zookeeper) para desacoplamento e alta taxa de ingestão.

## 🏗️ Fluxo Arquitetural e Responsabilidades (Explicitamente Event-Driven)
O fluxo de dados deve seguir rigorosamente a separação abaixo através de tópicos Kafka:

1. **Camada de Ingestão (Produtor - Fire and Forget)**:
   - O endpoint REST recebe o payload do ESP32: `{ "plate": "STRING", "weight": DOUBLE }`.
   - **Processamento Assíncrono**: O Controller delega o dado imediatamente para uma Thread dedicada (`@Async` ou Executor de Threads personalizado).
   - A Thread envia o dado para um **Tópico do Apache Kafka** e retorna o status `202 Accepted` para o ESP32 instantaneamente, sem travar o dispositivo.

2. **Camada de Processamento (Consumidor - Assíncrono)**:
   - Um serviço consumidor dedicado (`@KafkaListener`) lê as mensagens do tópico de forma contínua.
   - Esta camada executa as validações de cadastro, roda o **Algoritmo de Estabilização** e calcula as margens de lucro.
   - Após a estabilização ser confirmada, os dados consolidados são persistidos no banco de dados PostgreSQL.


## 🏗️ Arquitetura e Estrutura (Clean Architecture)
O projeto deve ser dividido estritamente em quatro camadas isoladas:
1. **Domain (Núcleo)**: Entidades de negócio e regras puras. Sem dependências do Spring ou JPA.
2. **Use Cases (Aplicação)**: Regras de negócio da aplicação. Define interfaces (Ports) de entrada e saída.
3. **Adapters (Interface/Infra)**: Controllers REST (Inbound) e Repositories JPA (Outbound). Mapeie entidades de banco para entidades de domínio.
4. **Frameworks/Config**: Configurações do Spring, Beans, Segurança e inicialização do PostgreSQL.

---

## 📋 Requisitos do Desafio (Diretrizes de Implementação)

### 1. Modelagem e Cadastros (Domain & JPA)
Implemente as entidades de domínio e persistência para:
- **Filial**: Identificação das filiais pelo Brasil.
- **Balança**: Associada a uma filial e identificada pelo ID enviado ou token.
- **Tipo de Grão**: Contém preço de compra por tonelada e estoque atual na doca.
- **Caminhão**: Contém placa e peso de Tara de fábrica.
- **Transação de Transporte**: Ciclo de vida completo (Início na partida da filial, definição do Tipo de Grão a buscar, Retorno/Pesagem na doca e Fim pós-descarregamento).

### 2. Recepção dos Dados (Endpoints HTTP)
- Crie o endpoint REST preparado para receber requisições de múltiplas balanças simultaneamente com o payload do ESP32: `{ "id": "STRING", "plate": "STRING", "weight": DOUBLE }`.
- Garanta que nenhuma lógica de negócio pesada ou acesso ao banco de dados seja executado dentro da thread principal da requisição HTTP (apenas envio ao Kafka).
- **Concorrência**: O endpoint deve ser thread-safe e capaz de processar requisições simultâneas de múltiplas balanças sem bloqueios (use `@Async`, pools de threads dedicados ou estruturas não-bloqueantes).
- **Safety**: All REST endpoints should be protected with JWT and OAuth2.

### 3. Algoritmo de Estabilização e Persistência (Dentro do Consumer)
- **Estratégia de Estabilização**: O consumidor deve analisar a série temporal dos envios por placa. O peso é considerado estabilizado quando a variação (delta) entre as últimas *X* leituras for menor que um limiar operacional *Y* por um intervalo mínimo de tempo (ex: 2 segundos).
- **Dados Estabilizados**: Salve na Transação apenas quando a estabilização for detectada:
  - Placa, Id da Balança, Tipo de Grão, Data/Hora da pesagem.
  - Peso Bruto Estabilizado, Tara (do cadastro do Caminhão) e Peso Líquido (Bruto - Tara).
  - Custo da Carga (Peso Líquido em toneladas * Preço de Compra).
  - Cálculo de Margem Dinâmica: Entre 5% e 20%, inversamente proporcional à quantidade disponível do grão na doca (Mais escasso = Maior margem).

### 4. Relatórios e Estatísticas (Queries Administrativas)
- Lucratividade média por Tipo de Grão (Preço de Venda com Margem Dinâmica vs Preço de Compra).
- Volume total de grãos movimentados por Filial e por período.
- Eficiência operacional das balanças (tempo médio de estabilização e fluxo de caminhões).

### 5. Segurança, Idempotência e Resiliência
- **Autenticação de Balanças**: Valide as requisições do ESP32 usando um token estático simples no Header (ex: `X-Balance-Token`).
- **Idempotência**: Implemente chaves de idempotência ou controle de estado na Transação para ignorar leituras remanescentes enviadas pelo ESP32 após a balança já ter estabilizado o peso daquele ciclo.
- **Idempotência e Retentativas**: Como a comunicação é *Fire-and-Forget*, inclua uma estratégia para ignorar leituras duplicadas ou flutuações remanescentes após o peso já ter sido estabilizado para aquela Transação ativa.
- **Resiliência do Kafka**: Garanta que em caso de falha temporária no banco PostgreSQL, o consumidor Kafka faça retentativas sem perder os dados enviados pelas balanças.


## 💻 Padrões de Código e Qualidade
- Use **Records** para DTOs de entrada e payloads de API (imutabilidade).
- Evite anotações do Lombok (`@Data`, `@Entity`) em classes do Domínio Puro.
- Trate exceções de negócio na camada de Use Cases e use um `@ControllerAdvice` para formatar as respostas de erro.
- Escreva testes unitários obrigatórios para o Algoritmo de Estabilização e Cálculo de Margem Dinâmica usando **JUnit 5** e **Mockito**.

## 🐳 Diretrizes de Deploy e Containerização (Docker / Docker Compose)

### 1. Dockerfile (Aplicação Spring Boot)
- **Multi-stage Build**: Utilize builds em duas etapas para garantir uma imagem final leve e segura.
  - Etapa 1 (Build): Imagem oficial do Maven/Gradle com **Java 21** para compilar o `.jar`.
  - Etapa 2 (Run): Imagem base Eclipse Temurin ou Amazon Corretto **Java 21 JRE** (Alpine ou Slim).
- **Segurança**: Crie e utilize um usuário não-root dentro do container para executar a aplicação.
- **Otimização**: Configure os parâmetros de memória da JVM (`JAVA_OPTS`) adequados para o container.

### 2. Docker Compose (Ambiente de Infraestrutura)
Crie um arquivo `docker-compose.yml` na raiz do projeto contendo os seguintes serviços interligados:
- **Application**: O container da sua API Spring Boot, configurado para aguardar a inicialização do PostgreSQL e do Kafka.
- **PostgreSQL**: 
  - Banco de dados relacional principal com suporte a JSONB, Full-Text Search e outras extensões modernas.
  - Configure credenciais, volumes nomeados para persistência de dados e health checks.
  - Utilize migrations automatizadas com **Flyway** para versionamento do schema.
- **Apache Kafka (Modo KRaft)**: 
  - **Proibido o uso de Zookeeper**: O Kafka deve ser configurado obrigatoriamente no modo **KRaft** (Kafka Raft Metadata Mode).
  - Utilize imagens oficiais atualizadas (como Apache Kafka oficial) que suportem KRaft nativamente.
  - Configure as variáveis de cluster necessárias (como `KAFKA_NODE_ID`, `KAFKA_PROCESS_ROLES=broker,controller` e `KAFKA_CONTROLLER_QUORUM_VOTERS`).
  - Crie automaticamente o tópico de pesagens das balanças durante a inicialização.
- **Persistência de Dados**: Garanta o uso de volumes Docker para manter os dados do PostgreSQL e metadados/logs do Kafka salvos.
- **Rede Isolada**: Todos os containers devem rodar em uma mesma subnet (`bridge network`) dedicada ao projeto.
