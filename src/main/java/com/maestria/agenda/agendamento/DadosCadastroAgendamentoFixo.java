package com.maestria.agenda.agendamento;

import com.maestria.agenda.agendamento.AgendamentoFixo.TipoRepeticao;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record DadosCadastroAgendamentoFixo(
    @NotNull Long clienteId,
    @NotNull Long profissionalId,
    @NotNull Long servicoId,
    @NotNull TipoRepeticao tipoRepeticao,
    @NotNull Integer intervaloRepeticao,
    Integer valorRepeticao,  
    @NotNull LocalDate dataInicio,
    LocalDate dataFim,      
    @NotNull LocalTime hora,
    @NotNull String duracao,
    String observacao,
    @NotNull Double valor
) {}