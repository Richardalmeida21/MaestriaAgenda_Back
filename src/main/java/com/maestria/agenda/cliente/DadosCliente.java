package com.maestria.agenda.cliente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record DadosCliente(
        @NotBlank(message = "Nome não pode ser vazio") String nome,
        @Email(message = "Email inválido") @NotBlank(message = "Email não pode ser vazio") String email,
        @NotBlank(message = "Telefone não pode ser vazio") String telefone
) {
}
