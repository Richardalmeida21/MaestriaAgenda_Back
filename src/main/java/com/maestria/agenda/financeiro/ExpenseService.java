package com.maestria.agenda.financeiro;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final Logger logger = LoggerFactory.getLogger(ExpenseService.class);

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public List<ExpenseResponseDTO> listarDespesas(LocalDate start, LocalDate end, String paidFilter) {
        List<Expense> expenses = expenseRepository.findByDateBetween(start, end);
        
        // Filter by paid status if not "all"
        if (!paidFilter.equalsIgnoreCase("all")) {
            Boolean isPaid = paidFilter.equalsIgnoreCase("paid");
            expenses = expenses.stream()
                    .filter(expense -> Objects.equals(expense.getPaid(), isPaid))
                    .collect(Collectors.toList());
        }
        
        return expenses.stream()
                .map(expense -> new ExpenseResponseDTO(
                        expense.getId(),
                        expense.getDescription(),
                        expense.getCategory(),
                        expense.getDate(),
                        expense.getAmount(),
                        expense.getPaid(),
                        expense.getRecurringExpenseId(),
                        null,
                        expense.getRecurringExpenseId() != null ? "RECURRING" : "REGULAR"
                ))
                .collect(Collectors.toList());
    }

    public void deletarDespesa(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new IllegalArgumentException("Despesa não encontrada com o ID: " + id);
        }
        expenseRepository.deleteById(id);
    }
    
    public ExpenseResponseDTO criarDespesa(ExpenseRequestDTO requestDTO) {
        try {
            Expense expense = new Expense();
            expense.setDescription(requestDTO.getDescription());
            expense.setCategory(requestDTO.getCategory());
            expense.setDate(requestDTO.getDate());
            expense.setAmount(requestDTO.getAmount());
            expense.setPaid(requestDTO.getPaid());
            
            Expense savedExpense = expenseRepository.save(expense);
            return new ExpenseResponseDTO(
                savedExpense.getId(),
                savedExpense.getDescription(),
                savedExpense.getCategory(),
                savedExpense.getDate(),
                savedExpense.getAmount(),
                savedExpense.getPaid(),
                null,
                null,
                "REGULAR"
            );
        } catch (Exception e) {
            logger.error("Erro ao criar despesa: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao criar despesa: " + e.getMessage());
        }
    }
    
    public ExpenseResponseDTO atualizarStatusPagamento(Long id, boolean paid) {
        try {
            Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Despesa não encontrada com ID: " + id));
            
            expense.setPaid(paid);
            Expense savedExpense = expenseRepository.save(expense);
            
            return new ExpenseResponseDTO(
                savedExpense.getId(),
                savedExpense.getDescription(),
                savedExpense.getCategory(),
                savedExpense.getDate(),
                savedExpense.getAmount(),
                savedExpense.getPaid(),
                savedExpense.getRecurringExpenseId(),
                null,
                savedExpense.getRecurringExpenseId() != null ? "RECURRING" : "REGULAR"
            );
        } catch (Exception e) {
            logger.error("Erro ao atualizar status de pagamento da despesa: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao atualizar status de pagamento: " + e.getMessage());
        }
    }
    
    /**
     * Verifica se uma instância de despesa fixa existe e está associada à despesa fixa informada
     */
    public boolean verificarSeInstanciaExiste(Long instanceId, Long recurringExpenseId) {
        try {
            // Buscar a despesa pelo ID
            Expense expense = expenseRepository.findById(instanceId)
                .orElse(null);
            
            // Verificar se existe e se pertence à despesa fixa indicada
            if (expense != null) {
                return expense.getRecurringExpenseId() != null && 
                       expense.getRecurringExpenseId().equals(recurringExpenseId);
            }
            return false;
        } catch (Exception e) {
            logger.error("Erro ao verificar instância: {}", e.getMessage(), e);
            return false;
        }
    }
}
