-- ============================================
-- ÍNDICES ADICIONAIS PARA OTIMIZAÇÃO
-- ============================================
-- Execute este SQL no Supabase SQL Editor
-- Data: 2026-02-25

-- Índice para busca de profissionais por login (usado no login)
CREATE INDEX IF NOT EXISTS idx_profissional_login 
  ON profissional(login);

-- Índice para servicos por categoria (usado em comissões)
CREATE INDEX IF NOT EXISTS idx_servico_categoria 
  ON servico(categoria_id);

-- Índice para comissao_profissional (busca de comissões por categoria)
CREATE INDEX IF NOT EXISTS idx_comissao_prof_categoria 
  ON comissao_profissional(profissional_id, categoria_id);

-- Índice para clientes (buscas frequentes por nome)
CREATE INDEX IF NOT EXISTS idx_cliente_nome 
  ON cliente(nome);

-- Índice para agendamento_servico (join frequente)
CREATE INDEX IF NOT EXISTS idx_agendamento_servico_agendamento 
  ON agendamento_servico(agendamento_id);

CREATE INDEX IF NOT EXISTS idx_agendamento_servico_servico 
  ON agendamento_servico(servico_id);

-- Atualizar estatísticas das tabelas
ANALYZE profissional;
ANALYZE servico;
ANALYZE comissao_profissional;
ANALYZE cliente;
ANALYZE agendamento_servico;

-- Verificar se os índices foram criados
SELECT 
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes 
WHERE schemaname = 'public' 
  AND indexname LIKE 'idx_%'
ORDER BY tablename, indexname;

-- Verificar tamanho dos índices
SELECT
    tablename AS table_name,
    indexname AS index_name,
    pg_size_pretty(pg_relation_size(indexrelid)) AS index_size
FROM pg_stat_user_indexes
WHERE schemaname = 'public'
ORDER BY pg_relation_size(indexrelid) DESC;
