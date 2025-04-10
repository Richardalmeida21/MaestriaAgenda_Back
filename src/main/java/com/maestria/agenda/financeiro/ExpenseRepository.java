package com.maestria.agenda.financeiro;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByDateBetween(LocalDate start, LocalDate end);
    List<Expense> findByIsFixoTrue();
    boolean existsByDateAndRecurringExpenseId(LocalDate date, Long recurringExpenseId);
    List<Expense> findByRecurringExpenseIdAndDateBetween(Long recurringExpenseId, LocalDate startDate, LocalDate endDate);
    List<Expense> findByRecurringExpenseId(Long recurringExpenseId);
    List<Expense> findByDescriptionAndCategoryAndAmount(String description, String category, Double amount);
    boolean existsByIdAndRecurringExpenseId(Long id, Long recurringExpenseId);
}