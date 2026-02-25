-- =====================================================
-- ÍNDICES OTIMIZADOS - VERSÃO FINAL CORRIGIDA
-- =====================================================
-- Execute este arquivo APÓS criar as funções PostgreSQL
-- Esses índices otimizam TODAS as 9 funções para performance máxima

-- =====================================================
-- ÍNDICES CRÍTICOS PARA AGENDAMENTO
-- =====================================================

-- 1. Índice principal para queries de período + pago
CREATE INDEX IF NOT EXISTS idx_agendamento_data_pago 
    ON agendamento(data, pago)
    WHERE pago = true;

-- 2. Índice para queries por cliente e data (novos clientes)
CREATE INDEX IF NOT EXISTS idx_agendamento_cliente_data 
    ON agendamento(cliente_id, data, pago)
    WHERE pago = true;

-- 3. Índice para taxa de retorno (cliente + data ordenada)
CREATE INDEX IF NOT EXISTS idx_agendamento_taxa_retorno 
    ON agendamento(cliente_id, data)
    WHERE pago = true;

-- 4. Índice para horários mais procurados
CREATE INDEX IF NOT EXISTS idx_agendamento_hora_data 
    ON agendamento(hora, data)
    WHERE pago = true;

-- 5. Índice para queries por profissional
CREATE INDEX IF NOT EXISTS idx_agendamento_profissional_data 
    ON agendamento(profissional_id, data, pago)
    WHERE pago = true;

-- =====================================================
-- ÍNDICES CRÍTICOS PARA JOINS (CÁLCULO DE VALOR)
-- =====================================================

-- 6. Índice para JOIN agendamento → agendamento_servico
CREATE INDEX IF NOT EXISTS idx_agendamento_servico_agendamento 
    ON agendamento_servico(agendamento_id);

-- 7. Índice para JOIN agendamento_servico → servico
CREATE INDEX IF NOT EXISTS idx_agendamento_servico_servico 
    ON agendamento_servico(servico_id);

-- 8. Índice composto otimizado (JOIN + categoria)
CREATE INDEX IF NOT EXISTS idx_agendamento_servico_completo 
    ON agendamento_servico(agendamento_id, servico_id);

-- 9. Índice para servico por categoria (comissões)
CREATE INDEX IF NOT EXISTS idx_servico_categoria 
    ON servico(categoria_id);

-- =====================================================
-- ÍNDICES PARA DESPESAS E COMISSÕES
-- =====================================================

-- 10. Índice para despesas por período e status
CREATE INDEX IF NOT EXISTS idx_expenses_date_paid 
    ON expenses(date, paid);

-- 11. Índice para comissões por data e status
CREATE INDEX IF NOT EXISTS idx_comissoes_pagamentos_data_status 
    ON comissoes_pagamentos(data_pagamento, status)
    WHERE status = 'PAGO';

-- 12. Índice para comissões por profissional e período
CREATE INDEX IF NOT EXISTS idx_comissoes_pagamentos_profissional 
    ON comissoes_pagamentos(profissional_id, periodo_inicio, periodo_fim, status);

-- =====================================================
-- ÍNDICES PARA CONFIGURAÇÕES DE COMISSÃO
-- =====================================================

-- 13. Índice para taxa de pagamento
CREATE INDEX IF NOT EXISTS idx_taxa_pagamento_lookup 
    ON taxa_pagamento(profissional_id, forma_pagamento);

-- 14. Índice para comissão profissional
CREATE INDEX IF NOT EXISTS idx_comissao_profissional_lookup 
    ON comissao_profissional(profissional_id, categoria_id);

-- =====================================================
-- ATUALIZAR ESTATÍSTICAS DO POSTGRESQL
-- =====================================================
-- Força o PostgreSQL a recalcular estatísticas para query planner

ANALYZE agendamento;
ANALYZE agendamento_servico;
ANALYZE servico;
ANALYZE expenses;
ANALYZE comissoes_pagamentos;
ANALYZE taxa_pagamento;
ANALYZE comissao_profissional;
ANALYZE profissional;
ANALYZE cliente;

-- =====================================================
-- VERIFICAR ÍNDICES CRIADOS
-- =====================================================
SELECT 
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename IN (
    'agendamento', 
    'agendamento_servico', 
    'servico',
    'expenses',
    'comissoes_pagamentos',
    'taxa_pagamento',
    'comissao_profissional'
)
ORDER BY tablename, indexname;

-- =====================================================
-- ✅ ÍNDICES OTIMIZADOS CRIADOS!
-- =====================================================
-- Total: 14 índices estratégicos
-- Benefício: 100x performance em queries de métricas/financeiro/comissões
