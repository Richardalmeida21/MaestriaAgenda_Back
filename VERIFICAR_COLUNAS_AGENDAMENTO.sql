-- Script para descobrir as colunas da tabela agendamento
SELECT 
    column_name,
    data_type,
    is_nullable
FROM information_schema.columns
WHERE table_name = 'agendamento'
ORDER BY ordinal_position;

-- Verificar também a estrutura completa
SELECT * FROM agendamento LIMIT 1;
