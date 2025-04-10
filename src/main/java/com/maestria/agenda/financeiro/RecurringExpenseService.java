package com.maestria.agenda.financeiro;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecurringExpenseService {
    
    private final RecurringExpenseRepository recurringExpenseRepository;
    private final ExpenseRepository expenseRepository;
    private final Logger logger = LoggerFactory.getLogger(RecurringExpenseService.class);
    
    public RecurringExpenseService(RecurringExpenseRepository recurringExpenseRepository,
                                  ExpenseRepository expenseRepository) {
        this.recurringExpenseRepository = recurringExpenseRepository;
        this.expenseRepository = expenseRepository;
    }
    
    /**
     * Lista todas as despesas fixas
     */
    public List<RecurringExpenseResponseDTO> listarDespesasFixas() {
        return recurringExpenseRepository.findAll().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Lista despesas fixas ativas
     */
    public List<RecurringExpenseResponseDTO> listarDespesasFixasAtivas() {
        return recurringExpenseRepository.findByActiveTrue().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Busca uma despesa fixa por ID
     */
    public RecurringExpenseResponseDTO buscarDespesaFixa(Long id) {
        RecurringExpense recurringExpense = recurringExpenseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Despesa fixa não encontrada com ID: " + id));
        
        return mapToDTO(recurringExpense);
    }
    
    /**
     * Cria uma nova despesa fixa
     */
    public RecurringExpenseCreationResponse criarDespesaFixa(RecurringExpenseRequestDTO requestDTO) {
        // Criar a despesa fixa
        RecurringExpense recurringExpense = new RecurringExpense();
        recurringExpense.setDescription(requestDTO.getDescription());
        recurringExpense.setCategory(requestDTO.getCategory());
        recurringExpense.setAmount(requestDTO.getAmount());
        recurringExpense.setRecurrenceType(requestDTO.getRecurrenceType());
        recurringExpense.setRecurrenceValue(requestDTO.getRecurrenceValue());
        recurringExpense.setStartDate(requestDTO.getStartDate());
        recurringExpense.setEndDate(requestDTO.getEndDate());
        recurringExpense.setActive(true);

        // Salvar a despesa fixa
        recurringExpense = recurringExpenseRepository.save(recurringExpense);

        // Gerar as despesas futuras
        List<Expense> generatedExpenses = generateFutureExpenses(recurringExpense);

        // Converter para DTOs
        List<ExpenseResponseDTO> expenseDTOs = generatedExpenses.stream()
            .map(expense -> new ExpenseResponseDTO(
                expense.getId(),
                expense.getDescription(),
                expense.getCategory(),
                expense.getDate(),
                expense.getAmount(),
                expense.getPaid(),
                true, // isFixo
                expense.getRecurringExpenseId(),
                expense.getEndDate()
            ))
            .collect(Collectors.toList());

        return new RecurringExpenseCreationResponse(
            mapToDTO(recurringExpense),
            expenseDTOs
        );
    }
    
    /**
     * Atualiza uma despesa fixa existente
     */
    public RecurringExpenseResponseDTO atualizarDespesaFixa(Long id, RecurringExpenseRequestDTO requestDTO) {
        try {
            RecurringExpense recurringExpense = recurringExpenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Despesa fixa não encontrada com ID: " + id));
            
            updateFromDTO(recurringExpense, requestDTO);
            
            RecurringExpense saved = recurringExpenseRepository.save(recurringExpense);
            logger.info("Despesa fixa atualizada com ID: {}", saved.getId());
            
            return mapToDTO(saved);
        } catch (Exception e) {
            logger.error("Erro ao atualizar despesa fixa: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao atualizar despesa fixa: " + e.getMessage());
        }
    }
    
    /**
     * Desativa (soft delete) uma despesa fixa
     */
    public void desativarDespesaFixa(Long id) {
        try {
            RecurringExpense recurringExpense = recurringExpenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Despesa fixa não encontrada com ID: " + id));
            
            recurringExpense.setActive(false);
            recurringExpenseRepository.save(recurringExpense);
            
            logger.info("Despesa fixa desativada com ID: {}", id);
        } catch (Exception e) {
            logger.error("Erro ao desativar despesa fixa: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao desativar despesa fixa: " + e.getMessage());
        }
    }
    
    /**
     * Exclui permanentemente uma despesa fixa
     */
    public void excluirDespesaFixa(Long id) {
        try {
            if (!recurringExpenseRepository.existsById(id)) {
                throw new RuntimeException("Despesa fixa não encontrada com ID: " + id);
            }
            
            recurringExpenseRepository.deleteById(id);
            logger.info("Despesa fixa excluída com ID: {}", id);
        } catch (Exception e) {
            logger.error("Erro ao excluir despesa fixa: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao excluir despesa fixa: " + e.getMessage());
        }
    }
    
    /**
     * Gera despesas para um período baseadas nas despesas fixas
     */
    @Transactional
    public List<ExpenseResponseDTO> gerarDespesasParaPeriodo(LocalDate inicio, LocalDate fim) {
        try {
            // Buscar todas as despesas fixas ativas que se aplicam ao período
            List<RecurringExpense> despesasFixas = 
                recurringExpenseRepository.findActiveRecurringExpensesInPeriod(inicio, fim);
            
            logger.info("Gerando despesas para o período de {} a {} com base em {} despesas fixas", 
                inicio, fim, despesasFixas.size());
            
            List<Expense> despesasGeradas = new ArrayList<>();
            
            // Para cada despesa fixa, gerar as ocorrências no período
            for (RecurringExpense despesaFixa : despesasFixas) {
                List<LocalDate> datasDespesa = calcularDatasNoIntervalo(despesaFixa, inicio, fim);
                
                for (LocalDate data : datasDespesa) {
                    // Verificar se a despesa já existe
                    boolean despesaExiste = expenseRepository.existsByDateAndRecurringExpenseId(data, despesaFixa.getId());
                    
                    if (!despesaExiste) {
                        // Criar uma despesa normal para cada data de ocorrência
                        Expense despesa = new Expense(
                            despesaFixa.getDescription(),
                            despesaFixa.getCategory(),
                            data,
                            despesaFixa.getAmount(),
                            false, // Inicialmente não paga
                            despesaFixa.getId() // Referência à despesa recorrente
                        );
                        
                        despesasGeradas.add(despesa);
                    }
                }
            }
            
            // Salvar apenas as despesas novas
            List<Expense> salvas = expenseRepository.saveAll(despesasGeradas);
            logger.info("{} despesas geradas e salvas", salvas.size());
            
            // Buscar todas as despesas do período (incluindo as recém-geradas)
            List<Expense> todasDespesas = expenseRepository.findByDateBetween(inicio, fim);
            
            // Converter para DTO e retornar
            return todasDespesas.stream()
                .map(d -> {
                    RecurringExpense recurringExpense = null;
                    if (d.getRecurringExpenseId() != null) {
                        recurringExpense = recurringExpenseRepository.findById(d.getRecurringExpenseId())
                            .orElse(null);
                    }
                    
                    return new ExpenseResponseDTO(
                        d.getId(),
                        d.getDescription(),
                        d.getCategory(),
                        d.getDate(),
                        d.getAmount(),
                        d.getPaid(),
                        d.getRecurringExpenseId(),
                        recurringExpense != null ? recurringExpense.getRecurrenceInfo() : null,
                        d.getRecurringExpenseId() != null ? "RECURRING" : "REGULAR"
                    );
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Erro ao gerar despesas para o período: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar despesas para o período: " + e.getMessage());
        }
    }
    
    /**
     * Calcula as datas em que uma despesa fixa ocorre em um intervalo
     */
    private List<LocalDate> calcularDatasNoIntervalo(RecurringExpense despesa, LocalDate inicio, LocalDate fim) {
        List<LocalDate> datas = new ArrayList<>();
        
        // Ajustar datas de início e fim
        LocalDate startDate = despesa.getStartDate().isBefore(inicio) ? inicio : despesa.getStartDate();
        LocalDate endDate = despesa.getEndDate() != null && despesa.getEndDate().isBefore(fim) ? 
                            despesa.getEndDate() : fim;
                            
        if (startDate.isAfter(endDate)) {
            return datas; // Intervalo inválido
        }
        
        LocalDate dataAtual = startDate;
        while (!dataAtual.isAfter(endDate)) {
            boolean gerarDespesa = false;
            Integer recurrenceValue = despesa.getRecurrenceValue() != null ? despesa.getRecurrenceValue() : 1;
            
            switch (despesa.getRecurrenceType()) {
                case DAILY:
                    gerarDespesa = true;
                    break;
                    
                case WEEKLY:
                    // Para recorrência semanal, verificamos se o dia da semana está na máscara
                    // Dia da semana é 1 (segunda) a 7 (domingo) em Java
                    int diaDaSemana = dataAtual.getDayOfWeek().getValue();
                    // Ajustar para 0 (domingo) a 6 (sábado)
                    int diaBit = (diaDaSemana == 7) ? 0 : diaDaSemana;
                    gerarDespesa = (recurrenceValue & (1 << diaBit)) != 0;
                    break;
                    
                case MONTHLY:
                    if (recurrenceValue == -1) {
                        // Último dia do mês
                        gerarDespesa = dataAtual.getDayOfMonth() == dataAtual.lengthOfMonth();
                    } else {
                        // Dia específico do mês - usar o mesmo dia da data de início quando recorrenceValue é nulo
                        if (despesa.getRecurrenceValue() == null) {
                            gerarDespesa = dataAtual.getDayOfMonth() == startDate.getDayOfMonth();
                        } else {
                            gerarDespesa = recurrenceValue == dataAtual.getDayOfMonth();
                        }
                    }
                    break;
                    
                case YEARLY:
                    // Para simplicidade, consideramos o dia do ano
                    if (despesa.getRecurrenceValue() == null) {
                        // Se não tiver valor específico, usar o mesmo dia do ano da data de início
                        gerarDespesa = dataAtual.getDayOfYear() == startDate.getDayOfYear();
                    } else {
                        gerarDespesa = recurrenceValue == dataAtual.getDayOfYear();
                    }
                    break;
                
                default:
                    break;
            }
            
            if (gerarDespesa) {
                datas.add(dataAtual);
            }
            
            dataAtual = dataAtual.plusDays(1);
        }
        
        return datas;
    }
    
    /**
     * Converte entidade para DTO
     */
    private RecurringExpenseResponseDTO mapToDTO(RecurringExpense recurringExpense) {
        return new RecurringExpenseResponseDTO(
            recurringExpense.getId(),
            recurringExpense.getDescription(),
            recurringExpense.getCategory(),
            recurringExpense.getAmount(),
            recurringExpense.getRecurrenceType(),
            recurringExpense.getRecurrenceValue(),
            recurringExpense.getStartDate(),
            recurringExpense.getEndDate(),
            recurringExpense.getActive(),
            true // isFixo is always true for recurring expenses
        );
    }
    
    /**
     * Atualiza entidade a partir de um DTO
     */
    private void updateFromDTO(RecurringExpense entity, RecurringExpenseRequestDTO dto) {
        entity.setDescription(dto.getDescription());
        entity.setCategory(dto.getCategory());
        entity.setAmount(dto.getAmount());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setRecurrenceType(dto.getRecurrenceType());
        
        // Para recorrência mensal, se o valor for nulo, use o dia da data de início
        if (dto.getRecurrenceType() == RecurrenceType.MONTHLY && dto.getRecurrenceValue() == null) {
            // Use o dia da data de início como valor de recorrência
            entity.setRecurrenceValue(dto.getStartDate().getDayOfMonth());
            logger.info("Usando o dia da data de início ({}) como valor de recorrência mensal", 
                        dto.getStartDate().getDayOfMonth());
        } else {
            // Caso contrário, use o valor exato informado no DTO
            entity.setRecurrenceValue(dto.getRecurrenceValue());
        }
        
        entity.setActive(true);
    }
    
    /**
     * Atualiza o status de pagamento de uma despesa fixa e todas as suas instâncias no mês corrente
     */
    @Transactional
    public RecurringExpenseResponseDTO atualizarStatusPagamento(Long id, boolean paid) {
        try {
            // Buscar a despesa fixa
            RecurringExpense recurringExpense = recurringExpenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Despesa fixa não encontrada com ID: " + id));
            
            // Definir período do mês atual
            LocalDate today = LocalDate.now();
            LocalDate startOfMonth = today.withDayOfMonth(1);
            LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
            
            // Buscar todas as despesas instantâneas geradas a partir desta despesa fixa para o mês atual
            List<Expense> despesasDoMes = expenseRepository.findByRecurringExpenseIdAndDateBetween(
                id, startOfMonth, endOfMonth);
                
            logger.info("Atualizando status de pagamento para {} da despesa fixa ID={}: {} despesas afetadas", 
                paid ? "PAGO" : "PENDENTE", id, despesasDoMes.size());
                
            // Se não existirem despesas instantâneas para o mês atual,
            // vamos gerar as instâncias necessárias
            if (despesasDoMes.isEmpty()) {
                // Calcular datas para o mês atual baseado no padrão de recorrência
                List<LocalDate> datasNoMes = calcularDatasNoIntervalo(recurringExpense, startOfMonth, endOfMonth);
                
                if (!datasNoMes.isEmpty()) {
                    logger.info("Gerando {} instâncias de despesa para o mês atual", datasNoMes.size());
                    
                    for (LocalDate data : datasNoMes) {
                        Expense despesa = new Expense(
                            recurringExpense.getDescription(),
                            recurringExpense.getCategory(),
                            data,
                            recurringExpense.getAmount(),
                            paid,  // já com o status correto
                            recurringExpense.getId()
                        );
                        
                        expenseRepository.save(despesa);
                        despesasDoMes.add(despesa);
                    }
                } else {
                    logger.warn("Nenhuma data válida encontrada para o mês atual");
                    throw new RuntimeException("Não foi possível atualizar a despesa fixa pois não há instâncias para o mês atual");
                }
            } else {
                // Atualizar o status de pagamento de todas as instâncias
                for (Expense despesa : despesasDoMes) {
                    despesa.setPaid(paid);
                    expenseRepository.save(despesa);
                }
            }
            
            // Retornar a despesa fixa atualizada com o novo status
            RecurringExpenseResponseDTO dto = mapToDTO(recurringExpense);
            // Forçamos o status para refletir a alteração que acabamos de fazer
            dto.setPaid(paid);
            
            return dto;
        } catch (Exception e) {
            logger.error("Erro ao atualizar status de pagamento da despesa fixa: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao atualizar status de pagamento: " + e.getMessage());
        }
    }
    
    /**
     * Resetar status de pagamento de uma despesa fixa problemática e suas instâncias
     */
    @Transactional
    public RecurringExpenseResponseDTO resetarStatusPagamento(Long id) {
        try {
            // Buscar a despesa fixa
            RecurringExpense recurringExpense = recurringExpenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Despesa fixa não encontrada: " + id));
            
            // Atualizar o status para não pago
            recurringExpense.setPaid(false);
            
            // Salvar a despesa fixa atualizada
            RecurringExpense savedExpense = recurringExpenseRepository.save(recurringExpense);
            
            // Também buscar e atualizar todas as instâncias desta despesa fixa no mês atual
            LocalDate hoje = LocalDate.now();
            LocalDate inicioDaMes = hoje.withDayOfMonth(1);
            LocalDate fimDoMes = hoje.withDayOfMonth(hoje.lengthOfMonth());
            
            List<Expense> instancias = expenseRepository.findByRecurringExpenseIdAndDateBetween(
                id, inicioDaMes, fimDoMes);
            
            for (Expense instancia : instancias) {
                instancia.setPaid(false);
                expenseRepository.save(instancia);
            }
            
            logger.info("Resetado status de pagamento da despesa fixa ID {}: {} instâncias atualizadas", 
                id, instancias.size());
            
            return mapToDTO(savedExpense);
        } catch (Exception e) {
            logger.error("Erro ao resetar status de pagamento da despesa fixa: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Corrigir uma despesa fixa problemática e suas instâncias
     */
    @Transactional
    public boolean corrigirDespesaFixaProblematica(Long id) {
        try {
            RecurringExpense recurringExpense = recurringExpenseRepository.findById(id)
                .orElse(null);
            
            if (recurringExpense == null) {
                logger.warn("Despesa fixa não encontrada ao tentar corrigir: {}", id);
                return false;
            }
            
            // Realizar operações de correção:
            
            // 1. Verificar campos obrigatórios e preencher se necessário
            if (recurringExpense.getDescription() == null || recurringExpense.getDescription().isEmpty()) {
                recurringExpense.setDescription("Despesa Fixa #" + id);
            }
            
            if (recurringExpense.getCategory() == null || recurringExpense.getCategory().isEmpty()) {
                recurringExpense.setCategory("others");
            }
            
            if (recurringExpense.getRecurrenceType() == null) {
                recurringExpense.setRecurrenceType(RecurrenceType.MONTHLY);
            }
            
            if (recurringExpense.getStartDate() == null) {
                recurringExpense.setStartDate(LocalDate.now());
            }
            
            // 2. Resetar status de pagamento
            recurringExpense.setPaid(false);
            
            // 3. Garantir que está ativa
            recurringExpense.setActive(true);
            
            // Salvar correções
            recurringExpenseRepository.save(recurringExpense);
            
            // 4. Verificar e corrigir instâncias problemáticas
            List<Expense> instancias = expenseRepository.findByRecurringExpenseId(id);
            for (Expense instancia : instancias) {
                // Corrigir relação
                if (!id.equals(instancia.getRecurringExpenseId())) {
                    instancia.setRecurringExpenseId(id);
                }
                
                // Resetar status de pagamento
                instancia.setPaid(false);
                
                // Corrigir descrição se necessário
                if (instancia.getDescription() == null || instancia.getDescription().isEmpty()) {
                    instancia.setDescription(recurringExpense.getDescription());
                }
                
                // Corrigir valor
                if (instancia.getAmount() == null || instancia.getAmount() <= 0) {
                    instancia.setAmount(recurringExpense.getAmount());
                }
                
                expenseRepository.save(instancia);
            }
            
            logger.info("Despesa fixa ID {} corrigida com sucesso. {} instâncias atualizadas.", 
                id, instancias.size());
            
            return true;
        } catch (Exception e) {
            logger.error("Erro ao corrigir despesa fixa problemática: {}", e.getMessage(), e);
            return false;
        }
    }

    private List<Expense> generateFutureExpenses(RecurringExpense recurringExpense) {
        List<Expense> expenses = new ArrayList<>();
        LocalDate currentDate = recurringExpense.getStartDate();
        LocalDate endDate = recurringExpense.getEndDate();

        while (!currentDate.isAfter(endDate)) {
            Expense expense = new Expense();
            expense.setDescription(recurringExpense.getDescription());
            expense.setCategory(recurringExpense.getCategory());
            expense.setAmount(recurringExpense.getAmount());
            expense.setDate(currentDate);
            expense.setPaid(false);
            expense.setRecurringExpenseId(recurringExpense.getId());
            expense.setIsFixo(true);
            expense.setEndDate(recurringExpense.getEndDate());
            expenses.add(expense);

            // Move to next occurrence based on recurrence type
            switch (recurringExpense.getRecurrenceType()) {
                case DAILY:
                    currentDate = currentDate.plusDays(1);
                    break;
                case WEEKLY:
                    currentDate = currentDate.plusWeeks(1);
                    break;
                case MONTHLY:
                    currentDate = currentDate.plusMonths(1);
                    break;
                case YEARLY:
                    currentDate = currentDate.plusYears(1);
                    break;
            }
        }

        return expenseRepository.saveAll(expenses);
    }
}
