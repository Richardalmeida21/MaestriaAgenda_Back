# Migração: Comissão por Serviço

## Resumo das Alterações

Este documento descreve as mudanças implementadas para migrar de um sistema de comissão global (70% para todos os serviços) para um sistema onde cada serviço individual tem sua própria comissão definida.

## Arquivos Modificados

### 1. Entidade Servico (`Servico.java`)
- **Adicionado**: Campo `comissaoPercentual` (Double)
- **Adicionado**: Validações: `@NotNull` e `@Positive`
- **Adicionado**: Getters e setters correspondentes

### 2. DTO DadosServico (`DadosServico.java`)
- **Adicionado**: Campo `comissaoPercentual` com validações

### 3. Controller ServicoController (`ServicoController.java`)
- **Modificado**: Métodos POST e PUT para incluir a comissão ao criar/atualizar serviços

### 4. Service ComissaoService (`ComissaoService.java`)
- **Removido**: Dependência da configuração global `comissao.percentual`
- **Modificado**: Métodos `calcularComissaoAgendamentosNormais()` e `calcularComissaoAgendamentosFixos()`
- **Novo comportamento**: Calcula comissão individual por serviço usando `servico.getComissaoPercentual()`

### 5. Controller AgendamentoController (`AgendamentoController.java`)
- **Removido**: Injeção da configuração `comissao.percentual` (não era usada)

### 6. Configuração (`application.properties`)
- **Removido**: `comissao.percentual=${COMISSAO_PERCENTUAL:70.0}`

### 7. Migração de Banco (`migration_comissao_por_servico.sql`)
- **Criado**: Script SQL para adicionar coluna `comissao_percentual` na tabela `servico`
- **Valor padrão**: 70% para serviços existentes (preservando comportamento anterior)

## Como Executar a Migração

### 1. Banco de Dados
Execute o script SQL antes de rodar a aplicação:
```sql
-- Adiciona a coluna comissao_percentual
ALTER TABLE servico ADD COLUMN comissao_percentual DOUBLE PRECISION;

-- Define 70% para serviços existentes
UPDATE servico SET comissao_percentual = 70.0 WHERE comissao_percentual IS NULL;

-- Torna obrigatório
ALTER TABLE servico ALTER COLUMN comissao_percentual SET NOT NULL;

-- Constraint de validação
ALTER TABLE servico ADD CONSTRAINT servico_comissao_positiva 
    CHECK (comissao_percentual > 0);
```

### 2. Aplicação
1. Pare a aplicação
2. Execute o script SQL acima
3. Atualize o código com as alterações
4. Reinicie a aplicação

## Testando as Mudanças

### 1. Criação de Novo Serviço
```json
POST /servico
{
    "nome": "Corte de Cabelo Premium",
    "valor": 50.0,
    "descricao": "Corte premium com profissional especializado",
    "duracao": "PT1H",
    "comissaoPercentual": 80.0
}
```

### 2. Atualização de Serviço Existente
```json
PUT /servico/{id}
{
    "nome": "Manicure",
    "valor": 25.0,
    "descricao": "Manicure tradicional",
    "duracao": "PT45M",
    "comissaoPercentual": 65.0
}
```

### 3. Verificação de Comissões
- Crie agendamentos com diferentes serviços
- Marque como pagos
- Consulte as comissões calculadas via endpoint de comissões
- Verifique se cada serviço está aplicando sua comissão específica

## Comportamento Antes vs Depois

### Antes
- **Comissão**: 70% para todos os serviços
- **Configuração**: Centralizada no `application.properties`
- **Flexibilidade**: Limitada - mesma comissão para todos

### Depois
- **Comissão**: Individual por serviço
- **Configuração**: No cadastro de cada serviço
- **Flexibilidade**: Total - cada serviço pode ter comissão diferente

## Impactos

### Positivos ✅
- Maior flexibilidade na definição de comissões
- Possibilidade de comissões diferenciadas por tipo de serviço
- Melhor controle financeiro

### A Considerar ⚠️
- Serviços existentes manterão 70% (comportamento anterior)
- Novos serviços devem ter comissão definida obrigatoriamente
- Interface do usuário precisará incluir campo de comissão

## Próximos Passos

1. **Interface Web**: Adicionar campo de comissão no formulário de cadastro de serviços
2. **Relatórios**: Atualizar relatórios para mostrar comissão por serviço
3. **Validações**: Considerar adicionar limites mínimos/máximos para comissões
4. **Histórico**: Implementar log de alterações de comissões

## Rollback (Se Necessário)

Caso precise reverter as alterações:

1. Restaure os arquivos originais
2. Execute:
```sql
ALTER TABLE servico DROP COLUMN IF EXISTS comissao_percentual;
```
3. Restaure a configuração no `application.properties`:
```properties
comissao.percentual=${COMISSAO_PERCENTUAL:70.0}
```
