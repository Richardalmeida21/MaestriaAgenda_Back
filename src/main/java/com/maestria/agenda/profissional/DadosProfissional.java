package com.maestria.agenda.profissional;

import jakarta.validation.constraints.NotBlank;

public record DadosProfissional(
        @NotBlank(message = "O nome não pode ser vazio.") String nome
) {
    // O record já fornece automaticamente o construtor, getters e outros métodos.
}
