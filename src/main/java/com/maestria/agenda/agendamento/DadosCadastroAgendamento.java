package com.maestria.agenda.agendamento;

import com.maestria.agenda.servicos.Servicos;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record DadosCadastroAgendamento(
        @NotNull Long clienteId,  // Agora usa ID do cliente
        @NotNull Long profissionalId, // Agora usa ID do profissional
        @NotNull Servicos servico,
        @NotNull @FutureOrPresent LocalDate data,
        @NotNull LocalTime hora
) {}
