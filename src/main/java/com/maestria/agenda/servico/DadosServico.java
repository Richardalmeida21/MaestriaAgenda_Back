package com.maestria.agenda.servico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DadosServico(
    @NotBlank(message = "Nome do serviço não pode ser vazio") 
    String nome,
    
    @NotNull(message = "Valor do serviço não pode ser nulo")
    @Positive(message = "Valor do serviço deve ser positivo") 
    Double valor,
    
    String descricao,
    
    @NotBlank(message = "Duração do serviço não pode ser vazia") 
    String duracao,
    
    @Positive(message = "Percentual de comissão deve ser positivo")
    Double comissaoPercentual // Temporariamente sem @NotNull para migração
) {}