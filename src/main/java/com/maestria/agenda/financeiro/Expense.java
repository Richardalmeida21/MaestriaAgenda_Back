package com.maestria.agenda.financeiro;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private Boolean paid = false;

    @Column(nullable = false)
    private Boolean isFixo = false;

    @Column
    private Integer dayOfMonth; // Used for fixed expenses to store the day of the month

    @Column
    private LocalDate endDate; // Used for fixed expenses to store when they should end

    @Column(name = "recurring_expense_id")
    private Long recurringExpenseId;

    public Expense(String description, String category, LocalDate date, Double amount, Boolean paid) {
        this.description = description;
        this.category = category;
        this.date = date;
        this.amount = amount;
        this.paid = paid;
    }

    public Expense(String description, String category, LocalDate date, Double amount, Boolean paid, Long recurringExpenseId) {
        this.description = description;
        this.category = category;
        this.date = date;
        this.amount = amount;
        this.paid = paid;
        this.recurringExpenseId = recurringExpenseId;
    }

    // Getters e Setters
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
}