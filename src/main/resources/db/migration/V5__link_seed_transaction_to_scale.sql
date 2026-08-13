-- A V3 original criou esta transacao de exemplo sem balanca_id. Sem o vinculo,
-- as leituras da BAL-001 nao encontram uma transacao ativa para serem persistidas.
UPDATE transacao_transporte
SET balanca_id = '123e4567-e89b-12d3-a456-426614174006',
    updated_at = CURRENT_TIMESTAMP
WHERE id = '123e4567-e89b-12d3-a456-426614174010'
  AND balanca_id IS NULL
  AND status = 'EM_ANDAMENTO';
