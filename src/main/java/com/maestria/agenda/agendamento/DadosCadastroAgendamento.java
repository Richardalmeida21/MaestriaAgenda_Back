package com.maestria.agenda.agendamento;

import com.maestria.agenda.servicos.Servicos;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record DadosCadastroAgendamento(
        @NotNull(message = "O ID do Cliente não pode ser nulo") Long clienteId,
        @NotNull(message = "O ID do Profissional não pode ser nulo") Long profissionalId,
        @NotNull Servicos servico,
        @NotNull @FutureOrPresent LocalDate data,
        @NotNull LocalTime hora,
        String observacao // Novo campo opcional
) {}
