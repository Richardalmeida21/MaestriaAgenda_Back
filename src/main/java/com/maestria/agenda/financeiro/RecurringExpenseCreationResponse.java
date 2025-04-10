package com.maestria.agenda.financeiro;

import java.util.List;

public class RecurringExpenseCreationResponse {
    private RecurringExpenseResponseDTO recurringExpense;
    private List<ExpenseResponseDTO> generatedExpenses;

    public RecurringExpenseCreationResponse(RecurringExpenseResponseDTO recurringExpense, 
                                          List<ExpenseResponseDTO> generatedExpenses) {
        this.recurringExpense = recurringExpense;
        this.generatedExpenses = generatedExpenses;
    }

    public RecurringExpenseResponseDTO getRecurringExpense() {
        return recurringExpense;
    }

    public List<ExpenseResponseDTO> getGeneratedExpenses() {
        return generatedExpenses;
    }
} 