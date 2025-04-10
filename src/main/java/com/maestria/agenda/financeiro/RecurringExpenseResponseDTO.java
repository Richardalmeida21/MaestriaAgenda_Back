package com.maestria.agenda.financeiro;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RecurringExpenseResponseDTO {
    private Long id;
    private String description;
    private String category;
    private Double amount;
    private LocalDate startDate;
    private LocalDate endDate;
    private RecurrenceType recurrenceType;
    private Integer recurrenceValue;
    private Boolean active;
    private String type;
    
    public RecurringExpenseResponseDTO(Long id, String description, String category, 
                                      Double amount, LocalDate startDate, LocalDate endDate,
                                      RecurrenceType recurrenceType, Integer recurrenceValue,
                                      Boolean active, String type) {
        this.id = id;
        this.description = description;
        this.category = category;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.recurrenceType = recurrenceType;
        this.recurrenceValue = recurrenceValue;
        this.active = active;
        this.type = type;
    }
    
    // Construtor sem o campo type para compatibilidade
    public RecurringExpenseResponseDTO(Long id, String description, String category, 
                                      Double amount, LocalDate startDate, LocalDate endDate,
                                      RecurrenceType recurrenceType, Integer recurrenceValue,
                                      Boolean active) {
        this(id, description, category, amount, startDate, endDate, recurrenceType, recurrenceValue, active, "RECURRING");
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getCategory() {
        return category;
    }
    
    public Double getAmount() {
        return amount;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public String getStartDateFormatted() {
        return startDate != null ? startDate.format(DateTimeFormatter.ISO_DATE) : null;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public String getEndDateFormatted() {
        return endDate != null ? endDate.format(DateTimeFormatter.ISO_DATE) : null;
    }
    
    public RecurrenceType getRecurrenceType() {
        return recurrenceType;
    }
    
    public Integer getRecurrenceValue() {
        return recurrenceValue;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public String getType() {
        return type;
    }
    
    // Informação formatada sobre recorrência
    public String getRecurrenceInfo() {
        if (recurrenceType == null) return "";
        
        switch (recurrenceType) {
            case DAILY:
                return "Diariamente";
            case WEEKLY:
                return formatWeeklyRecurrence();
            case MONTHLY:
                return formatMonthlyRecurrence();
            case YEARLY:
                return "Anualmente";
            default:
                return "";
        }
    }
    
    private String formatWeeklyRecurrence() {
        if (recurrenceValue == null) return "Semanalmente";
        
        StringBuilder days = new StringBuilder("Toda semana: ");
        String[] weekDays = {"Domingo", "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado"};
        boolean hasDays = false;
        
        for (int i = 0; i < 7; i++) {
            if ((recurrenceValue & (1 << i)) != 0) {
                if (hasDays) days.append(", ");
                days.append(weekDays[i]);
                hasDays = true;
            }
        }
        
        return hasDays ? days.toString() : "Semanalmente";
    }
    
    private String formatMonthlyRecurrence() {
        if (recurrenceValue == null) return "Mensalmente";
        
        if (recurrenceValue == -1) {
            return "Último dia do mês";
        } else {
            return "Todo dia " + recurrenceValue + " do mês";
        }
    }
}
