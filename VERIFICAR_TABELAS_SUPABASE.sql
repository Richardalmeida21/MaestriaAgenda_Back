-- =====================================================
-- VERIFICAR NOMES DAS TABELAS NO SUPABASE
-- =====================================================
-- Execute este script PRIMEIRO para ver os nomes corretos

-- 1. Listar todas as tabelas do schema public
SELECT 
    table_schema,
    table_name,
    table_type
FROM information_schema.tables 
WHERE table_schema = 'public'
  AND table_type = 'BASE TABLE'
ORDER BY table_name;

-- 2. Verificar colunas da tabela de agendamentos
SELECT 
    column_name,
    data_type,
    is_nullable
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name IN ('agendamento', 'Agendamento', 'AGENDAMENTO')
ORDER BY ordinal_position;

-- 3. Verificar se as tabelas estão em outro schema
SELECT 
    schemaname,
    tablename
FROM pg_tables
WHERE tablename LIKE '%agend%'
   OR tablename LIKE '%comiss%'
   OR tablename LIKE '%desp%';
