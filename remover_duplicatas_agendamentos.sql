-- Script para remover agendamentos duplicados mantendo apenas o mais antigo (menor ID)
-- Execute este script manualmente no seu banco de dados PostgreSQL

-- 1. Identificar duplicatas (agendamentos com mesmo cliente, profissional, serviço, data e hora)
WITH duplicatas AS (
    SELECT 
        id,
        cliente_id,
        profissional_id,
        servico_id,
        data,
        hora,
        ROW_NUMBER() OVER (
            PARTITION BY cliente_id, profissional_id, servico_id, data, hora 
            ORDER BY id ASC
        ) as rn
    FROM agendamento
    WHERE agendamento_fixo_id IS NULL -- Apenas agendamentos normais (não ocorrências de fixos)
)
-- 2. Deletar duplicatas (mantendo o primeiro registro - rn = 1)
DELETE FROM agendamento
WHERE id IN (
    SELECT id FROM duplicatas WHERE rn > 1
);

-- 3. Verificar quantos registros foram removidos
-- (Execute a query acima primeiro, depois esta para confirmar)
SELECT COUNT(*) as total_duplicatas_removidas 
FROM (
    SELECT 
        cliente_id,
        profissional_id,
        servico_id,
        data,
        hora,
        COUNT(*) - 1 as duplicatas
    FROM agendamento
    WHERE agendamento_fixo_id IS NULL
    GROUP BY cliente_id, profissional_id, servico_id, data, hora
    HAVING COUNT(*) > 1
) as duplicatas_restantes;
