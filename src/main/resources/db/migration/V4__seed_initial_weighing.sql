-- Dados de pesagem iniciais para testes, relatorios e validacoes de persistencia.
-- Mantido idempotente para evitar duplicacao caso o seed precise ser executado manualmente.
INSERT INTO pesagem (id, transacao_id, balanca_id, caminhao_id, tipo_grao_id, placa, peso_bruto_estabilizado, peso_liquido, data_hora_pesagem, custo_carga, status_estabilidade, observacao, created_at, updated_at)
SELECT '123e4567-e89b-12d3-a456-426614174011', '123e4567-e89b-12d3-a456-426614174010', '123e4567-e89b-12d3-a456-426614174006', '123e4567-e89b-12d3-a456-426614174004', '123e4567-e89b-12d3-a456-426614174002', 'ABC-1234', 35000.00, 25000.00, CURRENT_TIMESTAMP, 6250.00, 'ESTABILIZADA', 'Pesagem inicial seedada', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM pesagem WHERE id = '123e4567-e89b-12d3-a456-426614174011');
