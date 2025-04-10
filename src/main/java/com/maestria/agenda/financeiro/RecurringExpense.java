package com.maestria.agenda.financeiro;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "recurring_expenses")
public class RecurringExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    private String category;

    private Double amount;

    private LocalDate startDate;

    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private RecurrenceType recurrenceType;

    private Integer recurrenceValue; // Dia do mês (1-31) ou máscara de dias da semana

    private Boolean active;
    
    private Boolean paid = false;

    // Construtor padrão para JPA
    public RecurringExpense() {
        this.active = true;
        this.paid = false;
    }

    public RecurringExpense(String description, String category, Double amount,
            LocalDate startDate, LocalDate endDate,
            RecurrenceType recurrenceType, Integer recurrenceValue) {
        this.description = description;
        this.category = category;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.recurrenceType = recurrenceType;
        this.recurrenceValue = recurrenceValue;
        this.active = true;
        this.paid = false;
    }

    public String getRecurrenceInfo() {
        if (recurrenceType == null) {
            return "";
        }

        switch (recurrenceType) {
            case MONTHLY:
                return "Todo dia " + recurrenceValue + " do mês";
            case WEEKLY:
                StringBuilder dias = new StringBuilder();
                String[] diasSemana = { "Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb" };
                boolean primeiro = true;

                for (int i = 0; i < 7; i++) {
                    if ((recurrenceValue & (1 << i)) != 0) {
                        if (!primeiro) {
                            dias.append(", ");
                        }
                        dias.append(diasSemana[i]);
                        primeiro = false;
                    }
                }
                return "Semanal: " + dias.toString();
            case DAILY:
                return "Diariamente";
            case YEARLY:
                return "Anualmente";
            default:
                return recurrenceType.toString();
        }
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

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public RecurrenceType getRecurrenceType() {
        return recurrenceType;
    }

    public void setRecurrenceType(RecurrenceType recurrenceType) {
        this.recurrenceType = recurrenceType;
    }

    public Integer getRecurrenceValue() {
        return recurrenceValue;
    }

    public void setRecurrenceValue(Integer recurrenceValue) {
        this.recurrenceValue = recurrenceValue;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getPaid() {
        return paid;
    }
    
    public void setPaid(Boolean paid) {
        this.paid = paid;
    }
}
