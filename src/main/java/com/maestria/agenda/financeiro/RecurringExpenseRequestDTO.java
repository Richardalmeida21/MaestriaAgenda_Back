package com.maestria.agenda.financeiro;

import java.time.LocalDate;

public class RecurringExpenseRequestDTO {
    private String description;
    private String category;
    private Double amount;
    private LocalDate startDate;
    private LocalDate endDate;
    private RecurrenceType recurrenceType;
    private Integer recurrenceValue;
    
    public RecurringExpenseRequestDTO() {}
    
    // Getters e Setters
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public Double getAmount() {
        return amount;
    }
    
    public void setAmount(Double amount) {
        this.amount = amount;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public RecurrenceType getRecurrenceType() {
        return recurrenceType;
    }
    
    public void setRecurrenceType(RecurrenceType recurrenceType) {
        this.recurrenceType = recurrenceType;
    }
    
    public Integer getRecurrenceValue() {
        return recurrenceValue;
    }
    
    public void setRecurrenceValue(Integer recurrenceValue) {
        this.recurrenceValue = recurrenceValue;
    }
}
