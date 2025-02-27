package com.maestria.agenda.profissional;

public record DadosProfissional(
        String nome
) {
    // Construtor padrão
    public DadosProfissional() {
        this(null);
    }
}