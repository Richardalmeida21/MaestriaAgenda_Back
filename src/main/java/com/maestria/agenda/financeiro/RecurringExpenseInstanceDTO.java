package com.maestria.agenda.financeiro;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecurringExpenseInstanceDTO {
    private Long id;                 // ID da instância (null se ainda não existe no banco)
    private Long recurringExpenseId; // ID da despesa fixa de origem
    private String description;      // Descrição da despesa
    private String category;         // Categoria
    private Double amount;           // Valor
    private LocalDate dueDate;       // Data de vencimento
    private Boolean paid;            // Status de pagamento
    private Boolean exists;          // Se já existe no banco de dados
}