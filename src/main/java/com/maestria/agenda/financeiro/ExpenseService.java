package com.maestria.agenda.financeiro;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
                    .filter(expense -> expense.getPaid() == isPaid)
                    .collect(Collectors.toList());
        }
        
        return expenses.stream()
                .map(expense -> new ExpenseResponseDTO(
                        expense.getId(),
                        expense.getDescription(),
                        expense.getCategory(),
                        expense.getDate(),
                        expense.getAmount(),
                        expense.getPaid()
                ))
                .collect(Collectors.toList());
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
                savedExpense.getPaid()
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
                savedExpense.getPaid()
            );
        } catch (Exception e) {
            logger.error("Erro ao atualizar status de pagamento da despesa: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao atualizar status de pagamento: " + e.getMessage());
        }
    }
}