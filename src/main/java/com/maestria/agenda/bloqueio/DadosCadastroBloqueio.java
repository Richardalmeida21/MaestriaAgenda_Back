package com.maestria.agenda.bloqueio;

import java.time.LocalDate;
import java.time.LocalTime;

public record DadosCadastroBloqueio(
    Long profissionalId,
    LocalDate dataInicio,
    LocalDate dataFim,
    LocalTime horaInicio,
    LocalTime horaFim,
    boolean diaTodo,
    String motivo
) {}