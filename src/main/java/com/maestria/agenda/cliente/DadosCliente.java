package com.maestria.agenda.cliente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record DadosCliente(
        @NotBlank String nome,
        @Email @NotBlank String email,
        @NotBlank String telefone
) {
}
