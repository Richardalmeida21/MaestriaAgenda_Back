package com.maestria.agenda.agendamento;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record DadosCadastroAgendamento(
        @NotNull Long clienteId,
        @NotNull Long profissionalId,
        Long servicoId,  // Opcional - mantido para compatibilidade com código existente
        List<Long> servicoIds,  // Novo - para múltiplos serviços
        @NotNull @FutureOrPresent LocalDate data,
        @NotNull LocalTime hora,
        String observacao
) {
    // Método de conveniência para obter lista de IDs dos serviços
    public List<Long> getServiceIds() {
        if (servicoIds != null && !servicoIds.isEmpty()) {
            return servicoIds;
        }
        if (servicoId != null) {
            return List.of(servicoId);
        }
        return List.of();
    }
}
