package com.maestria.agenda.financeiro;

public class ExpensePaymentUpdateRequest {
    private boolean paid;
    
    // Construtor padrão para Jackson
    public ExpensePaymentUpdateRequest() {}
    
    public ExpensePaymentUpdateRequest(boolean paid) {
        this.paid = paid;
    }
    
    public boolean isPaid() {
        return paid;
    }
    
    public void setPaid(boolean paid) {
        this.paid = paid;
    }
}
