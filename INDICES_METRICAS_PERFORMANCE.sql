-- =====================================================
-- ÍNDICES PARA OTIMIZAÇÃO DE PERFORMANCE - MÉTRICAS
-- =====================================================
-- Executar no Supabase SQL Editor
-- Esses índices otimizam as queries do MetricsService

-- 1. Índice composto para queries de período (data BETWEEN)
-- Otimiza: findByDataBetween, calcularFaturamentoTotalPorPeriodo, etc.
CREATE INDEX IF NOT EXISTS idx_agendamento_data_status 
ON agendamento(data, status)
WHERE status IN ('CONFIRMADO', 'PAGO');

-- 2. Índice para contagem de clientes por período
-- Otimiza: contarTotalDeClientesPorPeriodo, contarNovosClientesPorPeriodo
CREATE INDEX IF NOT EXISTS idx_agendamento_data_cliente 
ON agendamento(data, cliente_id)
WHERE status IN ('CONFIRMADO', 'PAGO');

-- 3. Índice para agrupamento mensal (groupRevenueByMonth)
-- Otimiza queries que agrupam por MONTH(data)
CREATE INDEX IF NOT EXISTS idx_agendamento_month_year 
ON agendamento(EXTRACT(YEAR FROM data), EXTRACT(MONTH FROM data), status);

-- 4. Índice para horários mais procurados
-- Otimiza: findHorariosMaisProcurados
CREATE INDEX IF NOT EXISTS idx_agendamento_hora_data 
ON agendamento(hora, data, status)
WHERE status IN ('CONFIRMADO', 'PAGO');

-- 5. Índice para serviços mais agendados
-- Otimiza: findServicosMaisAgendados (via agendamento_servico)
CREATE INDEX IF NOT EXISTS idx_agendamento_servico_agendamento_data 
ON agendamento_servico(agendamento_id);

-- 6. Índice para despesas por período
-- Otimiza: calcularTotalDespesasPagas
CREATE INDEX IF NOT EXISTS idx_expenses_data_paga 
ON expenses(data_vencimento, paga)
WHERE paga = true;

-- 7. Índice para comissões pagas por período
-- Otimiza: calcularValorTotalPagoTodosProfissionaisNoPeriodo
CREATE INDEX IF NOT EXISTS idx_comissoes_pagamentos_data 
ON comissoes_pagamentos(data_pagamento, status)
WHERE status = 'PAGO';

-- 8. Índice composto crítico para taxa de retorno
-- Otimiza calcularTaxaRetorno (query mais pesada)
CREATE INDEX IF NOT EXISTS idx_agendamento_cliente_data_ordenado 
ON agendamento(cliente_id, data, status)
WHERE status IN ('CONFIRMADO', 'PAGO');

-- Atualizar estatísticas para o otimizador usar os novos índices
ANALYZE agendamento;
ANALYZE agendamento_servico;
ANALYZE expenses;
ANALYZE comissoes_pagamentos;

-- =====================================================
-- VERIFICAÇÃO DOS ÍNDICES CRIADOS
-- =====================================================
SELECT 
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes 
WHERE tablename IN ('agendamento', 'agendamento_servico', 'expenses', 'comissoes_pagamentos')
  AND indexname LIKE 'idx_%'
ORDER BY tablename, indexname;
