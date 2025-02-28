package com.maestria.agenda.agendamento;

import com.maestria.agenda.cliente.DadosCliente;
import com.maestria.agenda.profissional.DadosProfissional;
import com.maestria.agenda.servicos.Servicos;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;

import java.time.LocalDate;
import java.time.LocalTime;

public record DadosCadastroAgendamento(
        @NotNull DadosCliente cliente,
        @NotNull DadosProfissional profissional,
        @NotNull Servicos servico,
        @NotNull @FutureOrPresent LocalDate data,
        @NotNull LocalTime hora
) {
}