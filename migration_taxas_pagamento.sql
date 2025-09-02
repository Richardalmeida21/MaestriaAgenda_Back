-- =====================================================
-- MIGRAÇÃO: Taxas de Pagamento Configuráveis
-- Execute este script para criar a tabela de taxas configuráveis
-- =====================================================

-- 1. Cria a tabela taxa_pagamento
CREATE TABLE IF NOT EXISTS taxa_pagamento (
    id BIGSERIAL PRIMARY KEY,
    tipo_pagamento VARCHAR(20) NOT NULL UNIQUE,
    taxa DECIMAL(5,2) NOT NULL CHECK (taxa >= 0),
    ativo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Insere as taxas padrão (valores que estavam no enum)
INSERT INTO taxa_pagamento (tipo_pagamento, taxa, ativo) VALUES
('CREDITO_1X', 2.00, true),
('CREDITO_2X', 2.50, true),
('CREDITO_3X', 3.00, true),
('CREDITO_4X', 3.50, true),
('CREDITO_5X', 4.00, true),
('CREDITO_6X', 4.50, true),
('CREDITO_7X', 5.00, true),
('CREDITO_8X', 5.00, true),
('CREDITO_9X', 5.00, true),
('CREDITO_10X', 5.00, true),
('DEBITO', 1.50, true),
('PIX', 0.00, true),
('DINHEIRO', 0.00, true);

-- 3. Cria índices para performance
CREATE INDEX IF NOT EXISTS idx_taxa_pagamento_tipo ON taxa_pagamento(tipo_pagamento);
CREATE INDEX IF NOT EXISTS idx_taxa_pagamento_ativo ON taxa_pagamento(ativo);

-- 4. Adiciona comentários
COMMENT ON TABLE taxa_pagamento IS 'Configurações de taxas por tipo de pagamento';
COMMENT ON COLUMN taxa_pagamento.tipo_pagamento IS 'Tipo de pagamento (CREDITO_1X, DEBITO, PIX, etc)';
COMMENT ON COLUMN taxa_pagamento.taxa IS 'Percentual da taxa (ex: 2.50 para 2.5%)';
COMMENT ON COLUMN taxa_pagamento.ativo IS 'Se a configuração está ativa (true) ou usa padrão (false)';

-- 5. Verifica se a migração foi bem-sucedida
SELECT 'Migração concluída!' as status;
SELECT tipo_pagamento, taxa, ativo FROM taxa_pagamento ORDER BY tipo_pagamento;
