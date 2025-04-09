package com.maestria.agenda.financeiro;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RecurringExpenseRepository extends JpaRepository<RecurringExpense, Long> {
    
    /**
     * Encontra despesas recorrentes ativas que ocorrem em um período determinado
     * Uma despesa fixa está no período se:
     * - A data de início é anterior ao fim do período buscado E
     * - A data de fim é nula OU posterior ao início do período buscado
     */
    @Query("SELECT re FROM RecurringExpense re WHERE re.active = true " +
           "AND re.startDate <= :endDate " +
           "AND (re.endDate IS NULL OR re.endDate >= :startDate)")
    List<RecurringExpense> findActiveRecurringExpensesInPeriod(
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate);
    
    /**
     * Encontra todas as despesas recorrentes ativas
     */
    List<RecurringExpense> findByActiveTrue();
}
