-- V1__create_tables.sql
-- Sistema de Ingestao e Balancas de Graos — Schema (PostgreSQL + H2 compatible)
-- Referencia: .clinerules/entidades.md

CREATE TABLE filial (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    cidade VARCHAR(255),
    estado VARCHAR(100),
    ativo BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE tipo_grao (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    preco_compra_por_tonelada NUMERIC(12,2) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE caminhao (
    id UUID PRIMARY KEY,
    placa VARCHAR(20) NOT NULL UNIQUE,
    tara NUMERIC(10,2) DEFAULT 0 NOT NULL,
    descricao VARCHAR(255),
    ativo BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE balanca (
    id UUID PRIMARY KEY,
    codigo VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nome VARCHAR(255) NOT NULL,
    filial_id UUID NOT NULL,
    ativa BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_transacao_caminhao FOREIGN KEY (caminhao_id) REFERENCES caminhao(id),
    CONSTRAINT fk_transacao_filial FOREIGN KEY (filial_id) REFERENCES filial(id),
    CONSTRAINT fk_transacao_tipo_grao FOREIGN KEY (tipo_grao_id) REFERENCES tipo_grao(id),
    CONSTRAINT fk_transacao_balanca FOREIGN KEY (balanca_id) REFERENCES balanca(id)
);

CREATE TABLE doca (
    id UUID PRIMARY KEY,
    tipo_grao_id UUID NOT NULL,
    peso_disponivel NUMERIC(12,2) DEFAULT 0 NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_pesagem_transacao FOREIGN KEY (transacao_id) REFERENCES transacao_transporte(id),
    CONSTRAINT fk_pesagem_balanca FOREIGN KEY (balanca_id) REFERENCES balanca(id),
    CONSTRAINT fk_pesagem_caminhao FOREIGN KEY (caminhao_id) REFERENCES caminhao(id),
    CONSTRAINT fk_pesagem_tipo_grao FOREIGN KEY (tipo_grao_id) REFERENCES tipo_grao(id)
);
