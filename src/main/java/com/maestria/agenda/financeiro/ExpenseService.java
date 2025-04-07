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

    public List<ExpenseResponseDTO> listarDespesas(LocalDate start, LocalDate end, String status) {
        List<Expense> expenses = expenseRepository.findByDateBetween(start, end);
        if (!status.equalsIgnoreCase("all")) {
            expenses = expenses.stream()
                    .filter(expense -> expense.getStatus().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }
        return expenses.stream()
                .map(expense -> new ExpenseResponseDTO(
                        expense.getId(),
                        expense.getDescription(),
                        expense.getCategory(),
                        expense.getDate(),
                        expense.getAmount(),
                        expense.getStatus()
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
            expense.setStatus(requestDTO.getStatus());
            
            Expense savedExpense = expenseRepository.save(expense);
            return new ExpenseResponseDTO(
                savedExpense.getId(),
                savedExpense.getDescription(),
                savedExpense.getCategory(),
                savedExpense.getDate(),
                savedExpense.getAmount(),
                savedExpense.getStatus()
            );
        } catch (Exception e) {
            logger.error("Erro ao criar despesa: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao criar despesa: " + e.getMessage());
        }
    }
}