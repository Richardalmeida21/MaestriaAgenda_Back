-- Migração para suportar múltiplos serviços por agendamento (PostgreSQL/Supabase)
-- Criado em: 18/02/2026

-- 1. Criar tabela de relação entre agendamento e servicos
CREATE TABLE IF NOT EXISTS agendamento_servico (
    id BIGSERIAL PRIMARY KEY,
    agendamento_id BIGINT NOT NULL,
    servico_id BIGINT NOT NULL,
    ordem INT DEFAULT 0,
    CONSTRAINT fk_agendamento_servico_agendamento 
        FOREIGN KEY (agendamento_id) REFERENCES agendamento(id) ON DELETE CASCADE,
    CONSTRAINT fk_agendamento_servico_servico 
        FOREIGN KEY (servico_id) REFERENCES servico(id) ON DELETE CASCADE
);

-- Criar índices para melhor performance
CREATE INDEX IF NOT EXISTS idx_agendamento_servico_agendamento ON agendamento_servico(agendamento_id);
CREATE INDEX IF NOT EXISTS idx_agendamento_servico_servico ON agendamento_servico(servico_id);

-- 2. Migrar dados existentes (agendamentos que já têm servico_id)
-- Para cada agendamento existente, criar um registro na tabela agendamento_servico
INSERT INTO agendamento_servico (agendamento_id, servico_id, ordem)
SELECT id, servico_id, 0
FROM agendamento
WHERE servico_id IS NOT NULL
ON CONFLICT DO NOTHING;

-- 3. Nota: NÃO remova a coluna servico_id da tabela agendamento
-- Ela será mantida para compatibilidade com código legado
-- ALTER TABLE agendamento DROP COLUMN servico_id; -- NÃO EXECUTAR

-- 4. Opcional: Tornar a coluna servico_id nullable para novos agendamentos
-- PostgreSQL usa ALTER COLUMN ... DROP NOT NULL
ALTER TABLE agendamento ALTER COLUMN servico_id DROP NOT NULL;

-- Verificação: Mostrar quantos registros foram migrados
SELECT 
    'Agendamentos migrados' AS descricao,
    COUNT(*) AS quantidade
FROM agendamento_servico;
