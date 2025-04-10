package com.maestria.agenda.financeiro;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseRequestDTO {
    private String description;
    private String category;
    private LocalDate date;
    private Double amount;
    private Boolean isFixo;
    private Integer dayOfMonth; // For fixed expenses
    private LocalDate endDate; // For fixed expenses
}