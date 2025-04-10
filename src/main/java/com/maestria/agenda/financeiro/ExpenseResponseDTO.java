package com.maestria.agenda.financeiro;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ExpenseResponseDTO {
    private Long id;
    private String description;
    private String category;
    private LocalDate date;
    private Double amount;
    private Boolean paid;
    private Long recurringExpenseId;
    private String recurrenceInfo;

    public ExpenseResponseDTO() {}

    // Construtor completo com todos os campos
    public ExpenseResponseDTO(Long id, String description, String category, LocalDate date, Double amount, Boolean paid, Long recurringExpenseId, String recurrenceInfo) {
        this.id = id;
        this.description = description;
        this.category = category;
        this.date = date;
        this.amount = amount;
        this.paid = paid;
        this.recurringExpenseId = recurringExpenseId;
        this.recurrenceInfo = recurrenceInfo;
    }

    // Adicionando construtor simplificado que não exige recurringExpenseId e recurrenceInfo
    public ExpenseResponseDTO(Long id, String description, String category, LocalDate date, Double amount, Boolean paid) {
        this.id = id;
        this.description = description;
        this.category = category;
        this.date = date;
        this.amount = amount;
        this.paid = paid;
        this.recurringExpenseId = null;
        this.recurrenceInfo = null;
    }

    // Getters e Setters existentes
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getDateFormatted() {
        return date.format(DateTimeFormatter.ISO_DATE);
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

    public Long getRecurringExpenseId() {
        return recurringExpenseId;
    }

    public void setRecurringExpenseId(Long recurringExpenseId) {
        this.recurringExpenseId = recurringExpenseId;
    }

    public String getRecurrenceInfo() {
        return recurrenceInfo;
    }

    public void setRecurrenceInfo(String recurrenceInfo) {
        this.recurrenceInfo = recurrenceInfo;
    }
}