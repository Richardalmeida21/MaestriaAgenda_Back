-- ============================================
-- ÍNDICES DE PERFORMANCE - AGENDA MAESTRIA
-- ============================================
-- Versão simplificada - apenas tabelas essenciais
-- Criado em: 2026-02-04

-- ============================================
-- ÍNDICES PARA AGENDAMENTOS E COMISSÕES
-- ============================================

-- Índice para buscar agendamentos por profissional e data
CREATE INDEX IF NOT EXISTS idx_agendamento_profissional_data 
  ON agendamento(profissional_id, data);

-- Índice para buscar agendamentos por data e pago
CREATE INDEX IF NOT EXISTS idx_agendamento_data_pago 
  ON agendamento(data, pago);

-- Índice composto para queries de comissão
CREATE INDEX IF NOT EXISTS idx_agendamento_prof_data_pago 
  ON agendamento(profissional_id, data, pago);

-- Índice para agendamentos fixos
CREATE INDEX IF NOT EXISTS idx_agendamento_fixo_id 
  ON agendamento(agendamento_fixo_id) WHERE agendamento_fixo_id IS NOT NULL;

-- Índice para pagamentos de comissão por profissional e período
CREATE INDEX IF NOT EXISTS idx_comissoes_pagamentos_profissional_periodo 
  ON comissoes_pagamentos(profissional_id, periodo_inicio, periodo_fim);

-- Índice para pagamentos por status
CREATE INDEX IF NOT EXISTS idx_comissoes_pagamentos_status 
  ON comissoes_pagamentos(status);

-- Índice para buscar pagamentos por data
CREATE INDEX IF NOT EXISTS idx_comissoes_pagamentos_data 
  ON comissoes_pagamentos(data_pagamento);

-- ============================================
-- ÍNDICES PARA DESPESAS
-- ============================================

-- Índice para buscar despesas por data
CREATE INDEX IF NOT EXISTS idx_expenses_date 
  ON expenses(date);

-- Índice para buscar despesas por status de pagamento
CREATE INDEX IF NOT EXISTS idx_expenses_paid 
  ON expenses(paid);

-- Índice composto para despesas por data e status
CREATE INDEX IF NOT EXISTS idx_expenses_date_paid 
  ON expenses(date, paid);

-- Índice para despesas por categoria
CREATE INDEX IF NOT EXISTS idx_expenses_category 
  ON expenses(category);

-- ============================================
-- ATUALIZAR ESTATÍSTICAS
-- ============================================

ANALYZE agendamento;
ANALYZE comissoes_pagamentos;
ANALYZE expenses;
