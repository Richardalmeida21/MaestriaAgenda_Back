-- ==================================================
-- VERIFICAR ESTRUTURA COMPLETA DAS TABELAS
-- ==================================================

-- 1. Tabela servico (provavelmente tem preço/valor)
SELECT 
    column_name,
    data_type
FROM information_schema.columns
WHERE table_name = 'servico'
ORDER BY ordinal_position;

-- Amostra de dados
SELECT * FROM servico LIMIT 1;

-- ==================================================

-- 2. Tabela agendamento_servico (pode ter valor específico)
SELECT 
    column_name,
    data_type
FROM information_schema.columns
WHERE table_name = 'agendamento_servico'
ORDER BY ordinal_position;

-- Amostra de dados
SELECT * FROM agendamento_servico LIMIT 1;

-- ==================================================

-- 3. Verificar relacionamento completo
SELECT 
    a.id as agendamento_id,
    a.data,
    a.pago,
    s.nome as servico_nome,
    s.preco as servico_preco,
    s.valor as servico_valor,
    ags.valor as agendamento_servico_valor,
    ags.preco as agendamento_servico_preco
FROM agendamento a
LEFT JOIN agendamento_servico ags ON a.id = ags.agendamento_id
LEFT JOIN servico s ON ags.servico_id = s.id
LIMIT 3;
