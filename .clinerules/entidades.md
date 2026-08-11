# Instruções para gerar entidades JPA e tabelas PostgreSQL

## Objetivo
Este documento orienta LLMs a transformar os requisitos do sistema de pesagem de grãos em uma modelagem inicial de banco de dados e entidades JPA para um projeto Spring Boot.

## Premissas gerais
- O sistema usa UUID como chave primária em todas as entidades principais.
- Para PostgreSQL, recomenda-se usar o tipo UUID nativo para armazenar chaves primárias de forma segura e compatível com o padrão RFC 4122.
- As entidades do banco de dados serão criadas e mantidas pelo Flyway.
- Para cada mudança de schema, deve ser criado um arquivo de migração SQL versionado dentro da pasta de migrations do Flyway.
- Além das migrações, deve ser criado um arquivo de seed inicial com dados básicos para as entidades principais, como filial, tipo de grão, caminhão, balança e doca.
- Os nomes de tabelas devem ficar em snake_case.
- Os nomes de campos em Java devem seguir camelCase.
- O mapeamento entre banco e Java deve preservar o uso de UUID em JPA.
- O modelo deve suportar:
  - cadastros de caminhão, tipo de grão, filial, balança e transação de transporte;
  - recebimento das leituras da balança;
  - persistência apenas quando a pesagem estiver estabilizada;
  - controle do estoque disponível na doca;
  - cálculo de custo líquido, margem de lucro e análise administrativa.

## Regras de implementação recomendadas
- Use UUID como PK em todas as tabelas principais.
- Use `created_at` em todas as tabelas com histórico.
- Mantenha as alterações de schema sempre via Flyway, nunca via alteração manual direta no banco.
- Crie migrações separadas para estrutura e para seed, por exemplo: `V1__create_schema.sql` e `V2__seed_initial_data.sql`.
- Use `status` para controlar estados de transação e pesagem.
- Evite campos redundantes; use FKs explícitas para relacionamento.
- Para o fluxo de pesagem, mantenha o registro consolidado diretamente na tabela `pesagem`.

## Entidades recomendadas

### 1. Filial
Representa uma unidade operacional da empresa.

Atributos sugeridos:
- id: UUID PK
- nome: string
- cidade: string
- estado: string
- ativo: boolean
- created_at

Relacionamentos:
- Uma filial pode ter várias balanças.
- Uma filial pode ter várias transações de transporte.

### 2. TipoGrao
Representa o tipo de grão transportado.

Atributos sugeridos:
- id: UUID PK
- nome: string
- preco_compra_por_tonelada: decimal
- ativo: boolean
- created_at

Relacionamentos:
- Um tipo de grão pode aparecer em várias transações e pesagens.

### 3. Caminhao
Representa um caminhão cadastrado.

Atributos sugeridos:
- id: UUID PK
- placa: string unique
- tara: decimal
- descricao: string
- ativo: boolean
- created_at

Relacionamentos:
- Um caminhão pode ter várias transações de transporte.

### 4. Balanca
Representa a balança física instalada na filial.

Atributos sugeridos:
- id: UUID PK
- codigo: string unique
- password: string
- nome: string
- filial_id: UUID FK
- ativa: boolean
- created_at

Relacionamentos:
- Cada balança pertence a uma filial.
- Uma balança pode gerar várias pesagens.

### 5. TransacaoTransporte
Representa o ciclo de transporte de um caminhão, do início até o fim da pesagem.

Atributos sugeridos:
- id: UUID PK
- caminhão_id: UUID FK
- filial_id: UUID FK
- tipo_grao_id: UUID FK
- balanca_id: UUID FK nullable
- data_inicio: timestamp
- data_fim: timestamp nullable
- status: string
- created_at

Relacionamentos:
- Uma transação pertence a um caminhão, uma filial, um tipo de grão e opcionalmente uma balança.
- Uma transação pode ter várias pesagens.

### 6. Doca
Representa o estoque disponível de grãos na doca para venda, mantendo apenas o saldo atual de cada tipo de grão.

Atributos sugeridos:
- id: UUID PK
- tipo_grao_id: UUID FK
- peso_disponivel: decimal
- created_at

Relacionamentos:
- Uma doca está associada a um tipo de grão.
- Os demais valores serão calculados via SQL e disponibilizados por API.

### 7. Pesagem
Representa a pesagem consolidada, registrada somente quando o peso estiver estabilizado.

Atributos sugeridos:
- id: UUID PK
- transacao_id: UUID FK
- balanca_id: UUID FK
- caminhão_id: UUID FK nullable
- tipo_grao_id: UUID FK
- placa: string
- peso_bruto_estabilizado: decimal
- peso_liquido: decimal
- data_hora_pesagem: timestamp
- custo_carga: decimal
- status_estabilidade: string
- observacao: string nullable
- created_at

Relacionamentos:
- Uma pesagem pertence a uma transação e a uma balança.
- Pode estar relacionada a um caminhão e a um tipo de grão.

## Estrutura sugerida de tabelas em PostgreSQL
Abaixo segue um esqueleto baseado em PostgreSQL com UUID nativo.

```sql
CREATE TABLE filial (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    cidade VARCHAR(255),
    estado VARCHAR(100),
    ativo BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tipo_grao (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    preco_compra_por_tonelada NUMERIC(12,2) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE caminhao (
    id UUID PRIMARY KEY,
    placa VARCHAR(20) NOT NULL UNIQUE,
    tara NUMERIC(10,2) DEFAULT 0,
    descricao VARCHAR(255),
    ativo BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE balanca (
    id UUID PRIMARY KEY,
    codigo VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nome VARCHAR(255) NOT NULL,
    filial_id UUID NOT NULL,
    ativa BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_balanca_filial FOREIGN KEY (filial_id) REFERENCES filial(id)
);

CREATE TABLE transacao_transporte (
    id UUID PRIMARY KEY,
    caminhao_id UUID NOT NULL,
    filial_id UUID NOT NULL,
    tipo_grao_id UUID NOT NULL,
    balanca_id UUID,
    data_inicio TIMESTAMP NOT NULL,
    data_fim TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    observacao TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transacao_caminhao FOREIGN KEY (caminhao_id) REFERENCES caminhao(id),
    CONSTRAINT fk_transacao_filial FOREIGN KEY (filial_id) REFERENCES filial(id),
    CONSTRAINT fk_transacao_tipo_grao FOREIGN KEY (tipo_grao_id) REFERENCES tipo_grao(id),
    CONSTRAINT fk_transacao_balanca FOREIGN KEY (balanca_id) REFERENCES balanca(id)
);

CREATE TABLE doca (
    id UUID PRIMARY KEY,
    tipo_grao_id UUID NOT NULL,
    peso_disponivel NUMERIC(12,2) DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_doca_tipo_grao FOREIGN KEY (tipo_grao_id) REFERENCES tipo_grao(id)
);

CREATE TABLE pesagem (
    id UUID PRIMARY KEY,
    transacao_id UUID NOT NULL,
    balanca_id UUID NOT NULL,
    caminhao_id UUID,
    tipo_grao_id UUID NOT NULL,
    placa VARCHAR(20) NOT NULL,
    peso_bruto_estabilizado NUMERIC(12,2) NOT NULL,
    peso_liquido NUMERIC(12,2) NOT NULL,
    data_hora_pesagem TIMESTAMP NOT NULL,
    custo_carga NUMERIC(12,2),
    status_estabilidade VARCHAR(50) NOT NULL,
    observacao TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pesagem_transacao FOREIGN KEY (transacao_id) REFERENCES transacao_transporte(id),
    CONSTRAINT fk_pesagem_balanca FOREIGN KEY (balanca_id) REFERENCES balanca(id),
    CONSTRAINT fk_pesagem_caminhao FOREIGN KEY (caminhao_id) REFERENCES caminhao(id),
    CONSTRAINT fk_pesagem_tipo_grao FOREIGN KEY (tipo_grao_id) REFERENCES tipo_grao(id)
);
```

## Mapeamento JPA sugerido
Use entidades Java com UUID e anotações JPA padrão.

### Exemplo de padrão para UUID
```java
@Entity
@Table(name = "filial")
public class Filial {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "cidade")
    private String cidade;

    @Column(name = "estado")
    private String estado;

    @Column(name = "ativo")
    private Boolean ativo = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        this.createdAt = LocalDateTime.now();
    }
}
```

### Exemplo de relacionamento simples
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "filial_id", nullable = false)
private Filial filial;
```

### Exemplo de entidade Doca
```java
@Entity
@Table(name = "doca")
public class Doca {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_grao_id", nullable = false)
    private TipoGrao tipoGrao;

    @Column(name = "peso_disponivel")
    private BigDecimal pesoDisponivel = BigDecimal.ZERO;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        this.createdAt = LocalDateTime.now();
    }

    public void atualizarSaldo(BigDecimal novoPesoDisponivel) {
        this.pesoDisponivel = novoPesoDisponivel;
    }
}
```

### Regras para o mapeamento com UUID
- Use `UUID` na entidade Java.
- No banco PostgreSQL, armazene com o tipo nativo `UUID`.
- Para o Hibernate, o mapeamento costuma funcionar com `UUID` diretamente, sem necessidade de conversão manual.
- No Spring Boot, prefira usar `spring.jpa.hibernate.ddl-auto=validate` ou `update` apenas em ambiente de desenvolvimento.

## Estratégia de estabilização da pesagem
A LLM deve implementar uma regra de estabilização com base em janelas de medições.

### Estratégia recomendada
- Armazene as leituras recebidas da balança em uma estrutura temporária ou em um serviço de processamento.
- A cada nova leitura, calcule uma janela dos últimos valores de peso, por exemplo, os últimos 5 a 10 segundos.
- Identifique estabilidade quando:
  - a variação máxima dentro da janela for menor que um threshold configurável;
  - ou o desvio padrão for menor que um limite definido;
  - e o estado permanecer estável por um pequeno período de tempo.

### Regra prática
```text
Se a diferença entre o maior e o menor peso da janela for menor que 2kg
por 2 segundos consecutivos, considere a balança estabilizada.
```

### Dados a persistir quando estabilizado
- placa
- peso bruto estabilizado
- tara do cadastro do caminhão
- peso líquido = bruto - tara
- data/hora da pesagem
- id da balança
- tipo de grão
- custo da carga

### Regras para a doca
- A doca deve manter apenas o saldo disponível por tipo de grão.
- O saldo disponível deve ser atualizado via SQL ou serviço de aplicação.
- Os demais valores, como entrada total, quantidade vendida e margem aplicada, devem ser calculados por consulta SQL e expostos via API.
- O preço de venda e a margem de lucro devem ser calculados em um serviço Java e disponibilizados por endpoint de relatório.

## Regras de negócio que devem virar constraints ou validações
- O preço de venda deve ser calculado a partir do preço de compra com margem entre 5% e 20%.
- A margem deve variar inversamente com a quantidade disponível de grão na doca; isso pode ser tratado em uma regra de serviço ou em uma tabela de políticas de preço.
- O custo da carga deve ser calculado usando o peso líquido e o preço de compra do tipo de grão.
- A doca deve refletir o estoque disponível para venda, com atualização por recebimento e por venda parcial.
- A transação deve ter status como `EM_ANDAMENTO`, `FINALIZADA`, `CANCELADA`.
- A pesagem deve ter status como `PENDENTE`, `ESTABILIZADA`, `INVALIDA`.

## Prompt mestre para a LLM
Use este prompt para gerar o restante da implementação:

```text
Crie um modelo de domínio para um sistema de pesagem de grãos em Spring Boot.
Use UUID como chave primária para todas as entidades principais.
Crie as entidades JPA, os repositórios, os DTOs e os scripts DDL em PostgreSQL.
Crie também os arquivos de migração do Flyway para criar as tabelas e um seed inicial com dados básicos para filial, tipo de grão, caminhão, balança.
Considere as entidades: Filial, TipoGrao, Caminhao, Balanca, TransacaoTransporte, Doca e Pesagem.
Inclua relacionamentos adequados, campos de auditoria e suporte para persistir pesagens estabilizadas.
Use nomes em snake_case para tabelas e camelCase para atributos Java.
Aplique UUID como tipo nativo do PostgreSQL.
```

## Observação importante
Se o projeto for implementado com Spring Boot + Hibernate + PostgreSQL, a melhor abordagem inicial é:
- usar UUID nas entidades Java;
- mapear para o tipo `UUID` do banco;
- manter a lógica de estabilidade no serviço de aplicação;
- persistir apenas a pesagem estabilizada na tabela principal;
- manter a doca como entidade de estoque para controle de saldo disponível e vendas parciais.
