package com.maestria.agenda.financeiro;

import java.time.LocalDate;

public class ExpenseRequestDTO {
    private String description;
    private String category;
    private LocalDate date;
    private Double amount;
    private Boolean isFixo;
    private Integer dayOfMonth; // For fixed expenses
    private LocalDate endDate; // For fixed expenses

    public ExpenseRequestDTO() {}

    public ExpenseRequestDTO(String description, String category, LocalDate date, Double amount, Boolean isFixo, Integer dayOfMonth, LocalDate endDate) {
        this.description = description;
        this.category = category;
        this.date = date;
        this.amount = amount;
        this.isFixo = isFixo;
        this.dayOfMonth = dayOfMonth;
        this.endDate = endDate;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public Boolean getIsFixo() { return isFixo; }
    public void setIsFixo(Boolean isFixo) { this.isFixo = isFixo; }
    public Integer getDayOfMonth() { return dayOfMonth; }
    public void setDayOfMonth(Integer dayOfMonth) { this.dayOfMonth = dayOfMonth; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}