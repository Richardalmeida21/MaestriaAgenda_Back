package com.maestria.agenda.financeiro;

import java.time.LocalDate;

public class ExpenseRequestDTO {
    private String description;
    private String category;
    private LocalDate date;
    private Double amount;
    private String status;

    public ExpenseRequestDTO() {}

    public ExpenseRequestDTO(String description, String category, LocalDate date, Double amount, String status) {
        this.description = description;
        this.category = category;
        this.date = date;
        this.amount = amount;
        this.status = status;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}