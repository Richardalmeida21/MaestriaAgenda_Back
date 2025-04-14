package com.maestria.agenda.financeiro;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(name = "is_fixo", nullable = false)
    private Boolean isFixo = false;

    @Column(name = "day_of_month")
    private Integer dayOfMonth;

    @Column(name = "end_date")
    private LocalDate endDate;

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

    public Long getRecurringExpenseId() {
        return recurringExpenseId;
    }

    public void setRecurringExpenseId(Long recurringExpenseId) {
        this.recurringExpenseId = recurringExpenseId;
    }
}