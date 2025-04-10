package com.maestria.agenda.financeiro;

public class ExpensePaymentUpdateRequest {
    private boolean paid;
    private String paymentMethod;
    private String paymentDate;
    private boolean updateMainRecurring;
    
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
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getPaymentDate() {
        return paymentDate;
    }
    
    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }
    
    public boolean isUpdateMainRecurring() {
        return updateMainRecurring;
    }
    
    public void setUpdateMainRecurring(boolean updateMainRecurring) {
        this.updateMainRecurring = updateMainRecurring;
    }
}
