package com.maestria.agenda.financeiro;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String description;
    
    private String category;
    
    private LocalDate date;
    
    private Double amount;
    
    private String status;
    
    // Construtor padrão para JPA
    public Expense() {}

    public Expense(String description, String category, LocalDate date, Double amount, String status) {
        this.description = description;
        this.category = category;
        this.date = date;
        this.amount = amount;
        this.status = status;
    }

    // Getters e Setters
    public Long getId() {
        return id;
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