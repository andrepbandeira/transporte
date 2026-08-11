-- V3__seed_data.sql
-- Dados semestais iniciais: filial, tipo_grao, caminhao, balanca, doca, transacao_transporte
-- Idempotent: usa INSERT ... SELECT WHERE NOT EXISTS para suportar re-execucao em H2/PostgreSQL

-- === FILIAIS ===
INSERT INTO filial (id, nome, cidade, estado, ativo, created_at, updated_at)
SELECT '123e4567-e89b-12d3-a456-426614174000', 'Filial Sao Paulo', 'Sao Paulo', 'SP', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM filial WHERE id = '123e4567-e89b-12d3-a456-426614174000');

INSERT INTO filial (id, nome, cidade, estado, ativo, created_at, updated_at)
SELECT '123e4567-e89b-12d3-a456-426614174001', 'Filial Rio de Janeiro', 'Rio de Janeiro', 'RJ', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM filial WHERE id = '123e4567-e89b-12d3-a456-426614174001');

-- === TIPOS DE GRAO ===
INSERT INTO tipo_grao (id, nome, preco_compra_por_tonelada, ativo, created_at, updated_at)
SELECT '123e4567-e89b-12d3-a456-426614174002', 'Soja', 250.00, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tipo_grao WHERE id = '123e4567-e89b-12d3-a456-426614174002');

INSERT INTO tipo_grao (id, nome, preco_compra_por_tonelada, ativo, created_at, updated_at)
SELECT '123e4567-e89b-12d3-a456-426614174003', 'Milho', 120.00, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tipo_grao WHERE id = '123e4567-e89b-12d3-a456-426614174003');

-- === CAMINHOES ===
INSERT INTO caminhao (id, placa, tara, descricao, ativo, created_at, updated_at)
SELECT '123e4567-e89b-12d3-a456-426614174004', 'ABC-1234', 10000.00, 'Volvo FH 460', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM caminhao WHERE id = '123e4567-e89b-12d3-a456-426614174004');

INSERT INTO caminhao (id, placa, tara, descricao, ativo, created_at, updated_at)
SELECT '123e4567-e89b-12d3-a456-426614174005', 'XYZ-5678', 12000.00, 'Scania R450', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM caminhao WHERE id = '123e4567-e89b-12d3-a456-426614174005');

-- === BALANCAS ===
INSERT INTO balanca (id, codigo, password, nome, filial_id, ativa, created_at, updated_at)
SELECT '123e4567-e89b-12d3-a456-426614174006', 'BAL-001', '$2a$10$9xf4gRTbylWNRn1xw5myDu.Xjy/N4Zcfoatp85hpxgvM.sBNwiovC', 'Balanca SP-1', '123e4567-e89b-12d3-a456-426614174000', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM balanca WHERE id = '123e4567-e89b-12d3-a456-426614174006');

INSERT INTO balanca (id, codigo, password, nome, filial_id, ativa, created_at, updated_at)
SELECT '123e4567-e89b-12d3-a456-426614174007', 'BAL-002', '$2a$10$9xf4gRTbylWNRn1xw5myDu.Xjy/N4Zcfoatp85hpxgvM.sBNwiovC', 'Balanca RJ-1', '123e4567-e89b-12d3-a456-426614174001', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM balanca WHERE id = '123e4567-e89b-12d3-a456-426614174007');

-- === DOCAS ===
INSERT INTO doca (id, tipo_grao_id, peso_disponivel, created_at, updated_at)
SELECT '123e4567-e89b-12d3-a456-426614174008', '123e4567-e89b-12d3-a456-426614174002', 50000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM doca WHERE id = '123e4567-e89b-12d3-a456-426614174008');

INSERT INTO doca (id, tipo_grao_id, peso_disponivel, created_at, updated_at)
SELECT '123e4567-e89b-12d3-a456-426614174009', '123e4567-e89b-12d3-a456-426614174003', 30000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM doca WHERE id = '123e4567-e89b-12d3-a456-426614174009');

-- === TRANSACAO DE TRANSPORTE (exemplo) ===
INSERT INTO transacao_transporte (id, caminhao_id, filial_id, tipo_grao_id, balanca_id, data_inicio, data_fim, status, observacao, created_at, updated_at)
SELECT '123e4567-e89b-12d3-a456-426614174010', '123e4567-e89b-12d3-a456-426614174004', '123e4567-e89b-12d3-a456-426614174000', '123e4567-e89b-12d3-a456-426614174002', NULL, CURRENT_TIMESTAMP, NULL, 'EM_ANDAMENTO', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM transacao_transporte WHERE id = '123e4567-e89b-12d3-a456-426614174010');

