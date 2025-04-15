package com.maestria.agenda.financeiro;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    
    List<Expense> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
    boolean existsByIdAndRecurringExpenseId(Long id, Long recurringExpenseId);
    
    List<Expense> findByRecurringExpenseId(Long recurringExpenseId);
    
    boolean existsByDateAndRecurringExpenseId(LocalDate date, Long recurringExpenseId);
    
    List<Expense> findByRecurringExpenseIdAndDateBetween(Long recurringExpenseId, LocalDate startDate, LocalDate endDate);
}