package com.maestria.agenda.financeiro;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ComissaoResponseDTO {
    private Long profissionalId;
    private String nomeProfissional;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private Double comissaoTotal;
    private Double comissaoLiquida;  // 🔹 Novo campo para comissão após desconto
    private Double comissaoAgendamentosNormais;
    private Double comissaoAgendamentosFixos;

    public ComissaoResponseDTO(Long profissionalId, String nomeProfissional, 
                              LocalDate dataInicio, LocalDate dataFim,
                              Double comissaoTotal, Double comissaoLiquida,
                              Double comissaoAgendamentosNormais, 
                              Double comissaoAgendamentosFixos) {
        this.profissionalId = profissionalId;
        this.nomeProfissional = nomeProfissional;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.comissaoTotal = comissaoTotal;
        this.comissaoLiquida = comissaoLiquida;  // 🔹 Atribuindo o novo valor
        this.comissaoAgendamentosNormais = comissaoAgendamentosNormais;
        this.comissaoAgendamentosFixos = comissaoAgendamentosFixos;
    }

    // Getters e Setters
    public Long getProfissionalId() {
        return profissionalId;
    }
    
    public String getNomeProfissional() {
        return nomeProfissional;
    }
    
    public LocalDate getDataInicio() {
        return dataInicio;
    }
    
    public String getDataInicioFormatada() {
        return dataInicio.format(DateTimeFormatter.ISO_DATE);
    }
    
    public LocalDate getDataFim() {
        return dataFim;
    }
    
    public String getDataFimFormatada() {
        return dataFim.format(DateTimeFormatter.ISO_DATE);
    }
    
    public Double getComissaoTotal() {
        return comissaoTotal;
    }

    public Double getComissaoLiquida() {   // 🔹 Novo getter para a comissão líquida
        return comissaoLiquida;
    }

    public Double getComissaoAgendamentosNormais() {
        return comissaoAgendamentosNormais;
    }
    
    public Double getComissaoAgendamentosFixos() {
        return comissaoAgendamentosFixos;
    }
}
