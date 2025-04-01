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
) {
    // Construtor compacto que valida e corrige os dados
    public DadosCadastroBloqueio {
        // Se dataInicio for null, lança exceção
        if (dataInicio == null) {
            throw new IllegalArgumentException("Data de início não pode ser nula");
        }
        
        // Se dataFim for null, usa a mesma data de início
        if (dataFim == null) {
            dataFim = dataInicio;
        }
        
        // Para bloqueios de dia todo, define horários padrão se forem nulos
        if (diaTodo) {
            if (horaInicio == null) {
                horaInicio = LocalTime.of(0, 0);
            }
            if (horaFim == null) {
                horaFim = LocalTime.of(23, 59, 59);
            }
        }
    }
}