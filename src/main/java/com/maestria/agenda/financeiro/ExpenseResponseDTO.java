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

    public ExpenseResponseDTO(Long id, String description, String category, LocalDate date, Double amount, Boolean paid) {
        this.id = id;
        this.description = description;
        this.category = category;
        this.date = date;
        this.amount = amount;
        this.paid = paid;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDateFormatted() {
        return date.format(DateTimeFormatter.ISO_DATE);
    }

    public Double getAmount() {
        return amount;
    }

    public Boolean getPaid() {
        return paid;
    }
}