package com.maestria.agenda.financeiro;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    public ExpenseResponseDTO() {}

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

    public Boolean getIsFixo() {
        return isFixo;
    }

    public void setIsFixo(Boolean isFixo) {
        this.isFixo = isFixo;
    }

    public Integer getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(Integer dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}