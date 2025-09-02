-- Migração para adicionar campo comissao_percentual na tabela servico
-- Execute este script antes de rodar a aplicação com as novas alterações

-- Adiciona a coluna comissao_percentual
ALTER TABLE servico ADD COLUMN comissao_percentual DOUBLE PRECISION;

-- Define um valor padrão de 70% para todos os serviços existentes
-- (baseado no valor que estava configurado globalmente)
UPDATE servico SET comissao_percentual = 70.0 WHERE comissao_percentual IS NULL;

-- Torna a coluna obrigatória (NOT NULL)
ALTER TABLE servico ALTER COLUMN comissao_percentual SET NOT NULL;

-- Adiciona uma constraint para garantir que a comissão seja positiva
ALTER TABLE servico ADD CONSTRAINT servico_comissao_positiva 
    CHECK (comissao_percentual > 0);

-- Adiciona comentário para documentar o campo
COMMENT ON COLUMN servico.comissao_percentual IS 'Percentual de comissão específico para este serviço (exemplo: 70.0 para 70%)';
