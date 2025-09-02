-- =====================================================
-- COMANDOS SQL PARA EXECUTAR MANUALMENTE NO BANCO
-- Copie e cole cada bloco separadamente no seu cliente SQL
-- =====================================================

-- 1. PRIMEIRO: Criar a tabela taxa_pagamento
CREATE TABLE taxa_pagamento (
    id BIGSERIAL PRIMARY KEY,
    tipo_pagamento VARCHAR(20) NOT NULL UNIQUE,
    taxa DECIMAL(5,2) NOT NULL CHECK (taxa >= 0),
    ativo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. SEGUNDO: Inserir as taxas padrão (execute um INSERT por vez se der erro)
INSERT INTO taxa_pagamento (tipo_pagamento, taxa, ativo) VALUES ('CREDITO_1X', 2.00, true);
INSERT INTO taxa_pagamento (tipo_pagamento, taxa, ativo) VALUES ('CREDITO_2X', 2.50, true);
INSERT INTO taxa_pagamento (tipo_pagamento, taxa, ativo) VALUES ('CREDITO_3X', 3.00, true);
INSERT INTO taxa_pagamento (tipo_pagamento, taxa, ativo) VALUES ('CREDITO_4X', 3.50, true);
INSERT INTO taxa_pagamento (tipo_pagamento, taxa, ativo) VALUES ('CREDITO_5X', 4.00, true);
INSERT INTO taxa_pagamento (tipo_pagamento, taxa, ativo) VALUES ('CREDITO_6X', 4.50, true);
INSERT INTO taxa_pagamento (tipo_pagamento, taxa, ativo) VALUES ('CREDITO_7X', 5.00, true);
INSERT INTO taxa_pagamento (tipo_pagamento, taxa, ativo) VALUES ('CREDITO_8X', 5.00, true);
INSERT INTO taxa_pagamento (tipo_pagamento, taxa, ativo) VALUES ('CREDITO_9X', 5.00, true);
INSERT INTO taxa_pagamento (tipo_pagamento, taxa, ativo) VALUES ('CREDITO_10X', 5.00, true);
INSERT INTO taxa_pagamento (tipo_pagamento, taxa, ativo) VALUES ('DEBITO', 1.50, true);
INSERT INTO taxa_pagamento (tipo_pagamento, taxa, ativo) VALUES ('PIX', 0.00, true);
INSERT INTO taxa_pagamento (tipo_pagamento, taxa, ativo) VALUES ('DINHEIRO', 0.00, true);

-- 3. TERCEIRO: Criar índices para performance
CREATE INDEX idx_taxa_pagamento_tipo ON taxa_pagamento(tipo_pagamento);
CREATE INDEX idx_taxa_pagamento_ativo ON taxa_pagamento(ativo);

-- 4. QUARTO: Verificar se deu tudo certo
SELECT * FROM taxa_pagamento ORDER BY tipo_pagamento;

-- =====================================================
-- COMANDOS EXTRAS (se precisar)
-- =====================================================

-- Para deletar a tabela (se precisar refazer)
-- DROP TABLE IF EXISTS taxa_pagamento;

-- Para atualizar uma taxa específica (exemplo)
-- UPDATE taxa_pagamento SET taxa = 2.30 WHERE tipo_pagamento = 'CREDITO_1X';

-- Para desativar uma forma de pagamento
-- UPDATE taxa_pagamento SET ativo = false WHERE tipo_pagamento = 'CREDITO_10X';

-- Para reativar uma forma de pagamento
-- UPDATE taxa_pagamento SET ativo = true WHERE tipo_pagamento = 'CREDITO_10X';
