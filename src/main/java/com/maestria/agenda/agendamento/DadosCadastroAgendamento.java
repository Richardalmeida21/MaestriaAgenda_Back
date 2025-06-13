package com.maestria.agenda.agendamento;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;


public record DadosCadastroAgendamento(
        @NotNull Long clienteId,
        @NotNull Long profissionalId,
        @NotNull Long servicoId, 
        @NotNull @FutureOrPresent LocalDate data,
        @NotNull LocalTime hora,
        String observacao
) {}
