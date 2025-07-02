package com.maestria.agenda.financeiro;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    
    List<Expense> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
    boolean existsByIdAndRecurringExpenseId(Long id, Long recurringExpenseId);
    
    List<Expense> findByRecurringExpenseId(Long recurringExpenseId);
    
    boolean existsByDateAndRecurringExpenseId(LocalDate date, Long recurringExpenseId);
    
    List<Expense> findByRecurringExpenseIdAndDateBetween(Long recurringExpenseId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Calcula o valor total de despesas pagas em um período específico
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.date BETWEEN :startDate AND :endDate AND e.paid = true")
    Double calcularTotalDespesasPagas(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}