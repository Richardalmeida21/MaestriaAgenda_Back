package com.maestria.agenda.financeiro;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class ExpenseResponseDTO {
    private Long id;
    private String description;
    private String category;
    private LocalDate date;
    private Double amount;
    private Boolean paid;
    private Boolean isFixo;
    private Integer dayOfMonth;
    private LocalDate endDate;

    // Construtor completo com todos os campos
    public ExpenseResponseDTO(Long id, String description, String category, LocalDate date, Double amount, Boolean paid, Boolean isFixo, Integer dayOfMonth, LocalDate endDate) {
        this.id = id;
        this.description = description;
        this.category = category;
        this.date = date;
        this.amount = amount;
        this.paid = paid;
        this.isFixo = isFixo;
        this.dayOfMonth = dayOfMonth;
        this.endDate = endDate;
    }

    // Adicionando construtor simplificado que não exige recurringExpenseId e recurrenceInfo
    public ExpenseResponseDTO(Long id, String description, String category, LocalDate date, Double amount, Boolean paid) {
        this.id = id;
        this.description = description;
        this.category = category;
        this.date = date;
        this.amount = amount;
        this.paid = paid;
        this.isFixo = false;
        this.dayOfMonth = null;
        this.endDate = null;
    }

    public String getDateFormatted() {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public LocalDate getDate() { return date; }
}