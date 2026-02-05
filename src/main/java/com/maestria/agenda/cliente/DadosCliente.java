package com.maestria.agenda.cliente;

import jakarta.validation.constraints.NotBlank;

public record DadosCliente(
                @NotBlank(message = "Nome não pode ser vazio") String nome,
                String email,
                String telefone) {
}
