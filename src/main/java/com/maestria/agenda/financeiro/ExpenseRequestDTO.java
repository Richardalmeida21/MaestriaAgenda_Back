package com.maestria.agenda.financeiro;

import java.time.LocalDate;

public class ExpenseRequestDTO {
    private String description;
    private String category;
    private LocalDate date;
    private Double amount;
    private Boolean paid;

    public ExpenseRequestDTO() {}

    public ExpenseRequestDTO(String description, String category, LocalDate date, Double amount, Boolean paid) {
        this.description = description;
        this.category = category;
        this.date = date;
        this.amount = amount;
        this.paid = paid;
    }

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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Boolean getPaid() {
        return paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }
}