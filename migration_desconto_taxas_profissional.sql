-- =====================================================
-- MIGRAÇÃO: Adicionar controle de desconto de taxas por profissional
-- Execute cada comando separadamente no seu cliente SQL
-- =====================================================

-- 1. Adiciona a coluna descontar_taxas na tabela Profissional
ALTER TABLE Profissional ADD COLUMN descontar_taxas BOOLEAN;

-- 2. Atualiza todos os profissionais existentes para ter desconto ativado (comportamento atual)
UPDATE Profissional SET descontar_taxas = true;

-- 3. Verifica se a migração foi bem-sucedida
SELECT id, nome, descontar_taxas FROM Profissional ORDER BY nome;
