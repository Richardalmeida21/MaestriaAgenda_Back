package com.maestria.agenda.financeiro;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final Logger logger = LoggerFactory.getLogger(ExpenseService.class);

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public ExpenseResponseDTO createExpense(ExpenseRequestDTO requestDTO) {
        try {
            Expense expense = new Expense();
            expense.setDescription(requestDTO.getDescription());
            expense.setCategory(requestDTO.getCategory());
            expense.setAmount(requestDTO.getAmount());
            expense.setPaid(false);
            expense.setIsFixo(false);

            if (requestDTO.getIsFixo() != null && requestDTO.getIsFixo()) {
                expense.setIsFixo(true);
                expense.setDayOfMonth(requestDTO.getDayOfMonth());
                expense.setEndDate(requestDTO.getEndDate());
                
                // Create the first expense
                expense.setDate(LocalDate.now().withDayOfMonth(requestDTO.getDayOfMonth()));
                Expense savedExpense = expenseRepository.save(expense);
                
                // Generate future expenses
                generateFutureExpenses(savedExpense);
                
                return convertToDTO(savedExpense);
            } else {
                expense.setDate(requestDTO.getDate());
                Expense savedExpense = expenseRepository.save(expense);
                return convertToDTO(savedExpense);
            }
        } catch (Exception e) {
            logger.error("Error creating expense: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating expense: " + e.getMessage());
        }
    }

    private void generateFutureExpenses(Expense fixedExpense) {
        LocalDate currentDate = fixedExpense.getDate();
        LocalDate endDate = fixedExpense.getEndDate();
        
        while (currentDate.isBefore(endDate) || currentDate.isEqual(endDate)) {
            currentDate = currentDate.plusMonths(1);
            
            if (currentDate.isAfter(endDate)) {
                break;
            }
            
            Expense newExpense = new Expense();
            newExpense.setDescription(fixedExpense.getDescription());
            newExpense.setCategory(fixedExpense.getCategory());
            newExpense.setAmount(fixedExpense.getAmount());
            newExpense.setPaid(false);
            newExpense.setIsFixo(true);
            newExpense.setDayOfMonth(fixedExpense.getDayOfMonth());
            newExpense.setEndDate(fixedExpense.getEndDate());
            newExpense.setDate(currentDate);
            
            expenseRepository.save(newExpense);
        }
    }

    public List<ExpenseResponseDTO> listExpenses(LocalDate start, LocalDate end) {
        List<Expense> expenses = expenseRepository.findByDateBetween(start, end);
        return expenses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExpenseResponseDTO updatePaymentStatus(Long id, Boolean paid) {
        try {
            Expense expense = expenseRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Expense not found with ID: " + id));
            
            expense.setPaid(paid);
            Expense savedExpense = expenseRepository.save(expense);
            
            return convertToDTO(savedExpense);
        } catch (Exception e) {
            logger.error("Error updating payment status: {}", e.getMessage(), e);
            throw new RuntimeException("Error updating payment status: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteExpense(Long id) {
        try {
            if (!expenseRepository.existsById(id)) {
                throw new RuntimeException("Expense not found with ID: " + id);
            }
            expenseRepository.deleteById(id);
        } catch (Exception e) {
            logger.error("Error deleting expense: {}", e.getMessage(), e);
            throw new RuntimeException("Error deleting expense: " + e.getMessage());
        }
    }

    private ExpenseResponseDTO convertToDTO(Expense expense) {
        return new ExpenseResponseDTO(
            expense.getId(),
            expense.getDescription(),
            expense.getCategory(),
            expense.getDate(),
            expense.getAmount(),
            expense.getPaid(),
            expense.getIsFixo(),
            expense.getDayOfMonth(),
            expense.getEndDate()
        );
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
                        expense.getIsFixo(),
                        expense.getDayOfMonth(),
                        expense.getEndDate()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Verifica se uma instância de despesa fixa existe e está associada à despesa fixa informada
     */
    public boolean verificarSeInstanciaExiste(Long instanceId, Long recurringId) {
        return expenseRepository.existsByIdAndRecurringExpenseId(instanceId, recurringId);
    }
}
