package com.maestria.agenda.financeiro;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RecurringExpenseRepository extends JpaRepository<RecurringExpense, Long> {
    
    List<RecurringExpense> findByActiveTrue();
    
    @Query("SELECT r FROM RecurringExpense r WHERE " +
           "r.active = true AND " +
           "(r.startDate <= :endDate) AND " +
           "(r.endDate IS NULL OR r.endDate >= :startDate)")
    List<RecurringExpense> findActiveRecurringExpensesInPeriod(
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate);
}