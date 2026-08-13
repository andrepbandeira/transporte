# Desenvolvimento Assistido por IA — Sistema de Ingestão e Balanças de Grãos

> Documento de registro (exigido pelo edital: *“não é esperado simplesmente entregar o
> enunciado e deixar a IA fazer”*). Ele demonstra **como a IA foi usada de forma
> guiada, documentada e auditável** para construir este projeto, incluindo decisões,
> erros da IA e limitações enfrentadas. Baseia-se nos arquivos `prompt.md`, `cline.md`,
> `.clinerules/*.md` e, principalmente, no `memory-bank/` (fonte de verdade entre sessões).

---

## 1. Agente e modelo de IA

- **Agente**: [Cline](https://cline.bot) — agente de código autônomo com “memória que zera
  completamente entre sessões” (regra declarada em `.clinerules/memory-rules.md`).
- **Modelo de linguagem**: a runtime do Cline, por padrão, é alimentada pela família
  **Anthropic Claude** (provider default no cliente Cline). Importante: *o modelo exato e
  o tier (ex.: Claude-3.5/3.7 Sonnet, versão gratuita vs. paga) **não são expostos de
  forma confiável nesta sessão**.* O que é observável é o **comportamento** do agente:
  leitura/edição de arquivos, busca por código, execução de shell e chamadas HTTP.
- **Como foi usado**: a IA foi acionada **por mim (o/a candidato/a) com instruções
  estruturais** a cada fase (`prompt.md` → 9 fases). Ela **não decidiu sozinha o que
  construir**: segue um plano pré-definido, valida contra as regras do
  `.clinerules` e **registra tudo no `memory-bank`** para a próxima sessão continuar.

> O ponto proibido — e que este documento evita — é entregar apenas o enunciado e
> deixar a IA “fazer tudo”. Aqui a IA é **executor documentado**; o domínio e as
> decisões são do desenvolvedor.

---

## 2. Mecanismo de memória entre sessões (`memory-bank/`)

Como a memória do agente zera a cada sessão, o projeto depende de um **bank de
memória em arquivos markdown** que é **re-lido integralmente no início de cada
tarefa** (é uma regra, não recomendação). Essa foi a ponte que permitiu ao Cline
continuar este projeto após resets.

Estrutura usada:

| Arquivo | Função | Exemplo concreto neste projeto |
|---|---|---|
| `memory-bank/projectbrief.md` | Fonte de verdade do escopo | Sistema de ingestão de grãos: ESP32 → Kafka → estabilização → H2 |
| `memory-bank/productContext.md` | Por que existe / experiential | Ingestão 100 ms desacoplada, margem dinâmica 5–20 % |
| `memory-bank/activeContext.md` | Foco corrente + próximos passos | Estado das 9 fases, decisões ativas, notas de Windows/PowerShell |
| `memory-bank/systemPatterns.md` | Arquitetura + padrões críticos | Fluxo event-driven, ports/in-out, idempotência por placa |
| `memory-bank/techContext.md` | Stack + setup + constraints | Java 21, Boot 4.0.7, Maven, H2 (arquivo), KRaft |
| `memory-bank/progress.md` | O que funciona / falta / issues | 17/17 → 19/19 testes; Dockerfile; issues conhecidos |

**Como foi efetivamente usado nesta sessão**: no início, o agente leu *todos* esses
arquivos para reconstruir o estado (build verde 17/17, fases 0–8 concluídas, H2 já
em arquivo `./data/balancas`) — **sem isso, o contexto seria perdido** e a tarefa
(Flyway) seria refeita do zero ou, pior, conflitante.

### Limite imposta pela memória volátil
- A runtime **não persiste**: qualquer descoberta fora do `memory-bank` morre. Por isso
  decisões (ex.: “no Boot 4, Flyway precisa do starter dedicado”) **devem ser escritas
  de volta ao bank** antes de terminar a sessão — senão a próxima sessão as esquece.

---

## 3. Skills e regras (`.clinerules` + `.agents` + `.cline/skills`)

O `prompt.md` instrui a “ativar e carregar” skills. Neste repositório existem duas
famílias de guias de especialistas:

- **`.clinerules/`** — regras **sempre ativas** (não são invocadas; são injetadas de
  contexto):
  - `java-standards.md` (Java 21: records, pattern matching, text blocks; injeção por
    construtor; `ResponseEntity`; exceções específicas; Lazy/Sequenced; testes JUnit5 +
    Mockito + AssertJ; H2 em memória nos testes).
  - `memory-rules.md` (documentação obrigatória entre sessões).
- **`.agents/*.md`** — skills de especialista citadas no `prompt.md`:
  `code-reviewer`, `devops-engineer`, `docker-expert`, `java-architect`,
  `kubernetes-specialist`, `security-engineer`, `spring-boot-engineer` e
  `test-automator`.
- **`.cline/skills/`** — skills invocáveis via comando `/skill <nome>` (ex.:
  `/skill spring-boot-engineer`, `/skill jpa-patterns`, `/skill clean-code`).

> **Nota de honestidade operacional**: nesta sessão não invoquei a ferramenta
> `/skill` (nenhuma era necessária para a tarefa Flyway). O que guia-me diretamente é
> o `.clinerules/java-standards.md`, carregado automaticamente como contexto.
> As skills são um **mecanismo disponível**; nesta sessão não invoquei `/skill`, trabalhando
> direto de `java-standards.md` (sempre injetado) + memory‑bank + evidências empíricas.
>
> **Ajuste de precisão**: as regras **sempre-ativas** (`.clinerules/*.md` — incluindo
> `java-standards.md` e `memory-rules.md`) são injetadas como contexto em **toda** sessão
> e guiaram diretamente o código aqui (records, injeção por construtor, AssertJ, exceções
> específicas…). Já as **skills sob demanda** (`/skill <nome>`, em `.agents/` e
> `.cline/skills/`) são especialistas opcionais — o `prompt.md` as lista e o
> `memory-bank` registra que sessões **anteriores** (fases 0–8) carregaram
> `java-architect`/`spring-boot-patterns`. Em **nesta** sessão (tarefa Flyway) nenhuma
> skill foi invocada, e sim apenas validada via BOM/Maven Central/logs H2 para evitar
> decisões baseadas em pressupostos (ex.: o starter errado de Flyway).

## 4. Prompts de origem

- **`prompt.md`** — instrução estrutural inicial: ler `.clinerules`, carregar skills,
  confirmar em pt-BR o entendimento do cenário (100 ms / Kafka), desenhar a estrutura
  de pastas (Clean Architecture) e aguardar aprovação.
- **`cline.md`** — “AGENTS equivalent”: perfil (Arquiteto Sênior Java/Spring), stack
  (Java 21 + Spring Boot 3.x, H2, Maven, Kafka KRaft), fluxo event-driven (produtor
  fire-and-forget `202 Accepted`, consumer `@KafkaListener` com algoritmo de
  estabilização + margem dinâmica), requisitos de cadastro, relatórios, segurança
  (JWT + `X-Balance-Token`), idempotência e resiliência.
- **`.clinerules/java-standards.md`** — padrões de código (ver seção 3).

---

## 5. Como a IA coleta contexto — arquivos (markup) e busca

A IA não adivinha; ela **inspecciona** o código-fonte e **consulta fontes externas**
quando necessário. Ferramentas/técnicas usadas:

- `read_files` — leitura direta de fonte/yaml/testes e do `memory-bank`.
- `search_codebase` (regex) — localizar padrões (`flyway`, `schema.sql`,
  `ddl-auto`, `jdbc:h2`).
- `fetch_web_content` — verificar Maven Central / docs (decisivo: descobrir que
  **não existe `flyway-database-h2`** e confirmar que `spring-boot-starter-flyway`
  inclui `flyway-core`).
- `run_commands` — Maven, inspeção de arquivos e `git status` (PowerShell/Windows).

Exemplo de *markup* lido para decidir: o próprio `pom.xml` (Boot 4.0.7), o
`application.yaml`, as 5 JPA entities (`FilialJpaEntity`, … `TransacaoTransporteJpaEntity`)
e o enum `StatusTransacao` — tudo isso definiu o DDL exato da migração V1.

---

## 6. Passos principais do build (linha do tempo, segundo `memory-bank`)

> Fases 0–8 foram concluídas em sessões **anteriores**, documentadas no
> `memory-bank`. A sessão corrente acrescenta a **Fase 7.1 (Flyway)**.

| Fase | Objetivo | Status (do bank) |
|---|---|---|
| 0 | Inicializar `memory-bank` ao escopo real (Balanças de Grãos) | ✅ |
| 1 | Ajustar `pom.xml` (validation + oauth2-resource-server) | ✅ |
| 2 | Consolidar config em `application.yaml` (`.properties` removido) | ✅ |
| 3 | Domain puro (entidades, `AlgoritmoEstabilizacao`, `CalculadoraMargemDinamica`, exceções) + 12 testes unitários | ✅ |
| 4 | Application (ports in/out, use cases, DTOs records, `EstadoEstabilizacao`) | ✅ |
| 5 | Adapters (REST, Kafka produtor/consumer, JPA entities+repos+adapters) | ✅ |
| 6 | Config/Frameworks (Async, Kafka, Security JWT + token, OpenAPI, ExceptionHandler) | ✅ |
| 7 | **Build**: `./mvnw test` = 17/17 verde; `package` = BUILD SUCCESS | ✅ |
| 8 | Docker (Dockerfile multi-stage + docker-compose KRaft) | ✅ |
| **7.1 (ESTA SESSÃO)** | **Migrações versionadas com Flyway** | ✅ |

## 7. Esta sessão — Flyway: migrações versionadas

### Problema de partida
> “Trocamos o H2 de memória para **arquivo** (`jdbc:h2:file:./data/balancas`) e não
> há `data.sql`/`schema.sql`. Precisamos que a criação de tabelas seja **versionada**.”

O código existia só, mas **sem schema versionado**: as tabelas vinham da gambeta
`spring.jpa.hibernate.ddl-auto: update` — impossível de versionar/audit‑trail e
perigosa em produção.

### Decisões e mudanças aplicadas
1. **`pom.xml`** → adicionado `spring-boot-starter-flyway` (Boot 4.0.7 gerencia
   Flyway 11.14.1).
2. **`src/main/resources/db/migration/V1__create_tables.sql`** → cria as 5 tabelas
   espelhando **exatamente** as JPA entities: mesmas colunas, nullability, precisão
   (`NUMERIC(12,4)` …) e `UNIQUE(placa)`. `Instant → TIMESTAMP WITH TIME ZONE` para
   casar com o mapeamento Hibernate 7 (`TIMESTAMP_UTC`).
3. **`application.yaml`** → `ddl-auto: update → validate` (Flyway é dono do schema;
   Hibernate apenas valida) + bloco `spring.flyway` (`baseline-on-migrate: true` para
   DBs pré-existentes, `locations: classpath:db/migration`).
4. **`.gitignore`** → `data/` ignorada (DB em arquivo é local).
5. **`FlywayMigrationTest`** → teste de integração `@SpringBootTest` com `JdbcTemplate`
   que assegura: (a) V1 registrada em `flyway_schema_history` e (b) as 5 tabelas
   existem no `information_schema`.
6. **`memory-bank`** → `techContext.md`, `activeContext.md`, `progress.md` atualizados.

### Resultado verificado
```
[INFO] Tests run: 19, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS  (Total time: 14.239 s)
```
Log da Flyway: `Current version of schema "PUBLIC": 1` (migração aplicada e
idempotente — segundo run foi no-op e `validate` continuou OK).

## 8. Erros da IA, decisões revisadas e aprendizados (exigido pelo edital)

Este item demonstra **domínio do problema**, não automação cega. Três “loops”
críticos em que a IA errou e o plano foi corrigido com base em evidências:

| # | Erro / suposição da IA | Evidência | Correção tomada |
|---|---|---|---|
| 1 | Assumi que `org.flywaydb:flyway-core` bastava (padrão Boot 3.x). | `flyway-core` 11.14.1 foi baixado, mas a app **não executou migração**: nenhuma linha `Migrating schema`/`Current version`; Hibernate caiu em `missing table [balanca]`. | Investigado no BOM `spring-boot-dependencies-4.0.7.pom`: o autoconfig de Flyway foi movido para módulos separados em Boot 4. Consultei Maven Central (`spring-boot-starter-flyway`) e seu POM — que inclui `flyway-core`. Trocado o starter. |
| 2 | Assumi que H2 exigia `flyway-database-h2` (Flyway 10+ “split”). | Listagem Maven Central: **não existe** `flyway-database-h2`. | Confirmado: H2 continua embutido em `flyway-core`. Não adicionei artefato inexistente. |
| 3 | No teste, query `SELECT … FROM flyway_schema_history` falhou (`Table not found`). | H2 criou o histórico como identificador **quoted lowercase**: `Table "FLYWAY_SCHEMA_HISTORY" not found (candidates are: "flyway_schema_history")` → depois `Column "VERSION" not found`. | Ajustado para identificadores entre aspas duplas (`"flyway_schema_history"`, `"version"`, `"success"`). As tabelas do domínio são checadas via `information_schema` (case‑insensitive). |
| 4 | DDL de `Instant`: risco de validação Hibernate por tipo de coluna. | Hibernate 7 espera `TIMESTAMP_WITH_TIMEZONE` para `Instant`. | Usei `TIMESTAMP WITH TIME ZONE` (corresponde ao que o próprio Hibernate gera no H2) — `validate` passou. |

Além disso, a técnica de validação foi **evidencial, não autorreferencial**: rodei o
Maven e li o relatório real (1964 linhas de conditions report) em vez de adivinhar
por que o contexto não subia.

---

## 9. Limitações (modelos/tier/Tokens) — honesto

- **Modelo / tier não visível**: o agente não expõe qual Claude/Salesforce/Anthropic
  (versão e quota gratuita) está por trás. Qualquer afirmação sobre “contexto de 200 k
  tokens” é **estimativa lecionada**, não observada.
- **Contexto volátil = alto turnover**: entre resets, tudo é re-lido. Os arquivos
  grandes (log de boot de 1764 linhas, surefire reports) foram lidos **várias vezes**
  nesta sessão — isso **multiplica o consumo de tokens** sem valor novo. Em um tier
  gratuito com janela/contexto limitado, isso rapidamente estoura a quota.
- **Shell Windows (PowerShell) é árido para agentes**: redirects como `2>nul`,
  `mvnw` sem `.\`, e o fato de que o runner “mata” processos filhos quando emite a
  próxima chamada, **forçaram polling via WMI** (`Win32_Process.Create`) e arquivos de
  status — mais *round-trips* = mais tokens. (`memory-bank/activeContext.md` já
  registrava isso.)
- **Maven precisa baixar dependências**: a primeira compilação baixou
  `flyway-core` 11.14.1, `spring-boot-starter-flyway` e co‑transitivos — sem rede ou
  mirror offline, a build falha (não foi o caso aqui, mas é um risco de free tiers /
  CI sem cache).
- **Memória entre sessões é cara em tokens de “warm‑up”**: no começo de cada sessão
  o agente re-le todo o `memory-bank` e o `pom.xml`/`application.yaml` para
  reconstruir estado — repetição documentada, mas inevitável.
- **Limite de *tokens* por chamada de ferramenta**: pesquisas como `Select-String`
  sobre logs grandes precisam de chunking; erros de parse de pipe do PowerShell
  geraram iterações extras.

**Mitigações adotadas neste projeto (viáveis até em free tier):**
- Manter o `memory-bank` enxuto (markdown pequeno) e como **única fonte de verdade**
  entre resets — isso evita re-processar dezenas de arquivos toda sessão.
- Validar hipóteses com fontes externas (Maven Central / BOM) antes de editar, para
  não “gastar” ciclos de compile/test em correções evitáveis (ex.: artefato inexistente).
- Rodar a build **detached via WMI** para não bloquear/destruir o processo no runner.

---

## 10. Arquivos de apoio desta tarefa
- `pom.xml` — `+ spring-boot-starter-flyway`
- `src/main/resources/db/migration/V1__create_tables.sql` — migração V1
- `src/test/java/transporte/desafio/FlywayMigrationTest.java` — teste de migração
- `src/main/resources/application.yaml` — `ddl-auto: validate` + bloco `flyway`
- `.gitignore` — `+ data/`

---

## 11. Conclusão
O projeto foi **construído com IA sob supervisão e direção explícita**: um plano em 9
fases (definido por mim, o desenvolvedor), regras fixas no `.clinerules`, e um
`memory-bank` que sobrevive aos resets da runtime. A IA executou, pesquisou e
validou — e, quando errou (starter de Flyway, quoting do H2), **corrigiu com base em
evidências**, documentando cada passo. A entrega final: `BUILD SUCCESS` com **19/19
testes**, schema versionado e auditável pelo Flyway, pronto para evoluir com `V2__…`.