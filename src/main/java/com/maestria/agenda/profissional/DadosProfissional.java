package com.maestria.agenda.profissional;

import jakarta.validation.constraints.NotBlank;

public record DadosProfissional(
        @NotBlank String nome
) {
    // Construtor padrão
    public DadosProfissional() {
        this(null);
    }
}