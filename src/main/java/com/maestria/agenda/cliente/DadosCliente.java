package com.maestria.agenda.cliente;

public record DadosCliente(
        String nome,
        String email,
        String telefone
) {
    // Construtor padrão
    public DadosCliente() {
        this(null, null, null);
    }
}