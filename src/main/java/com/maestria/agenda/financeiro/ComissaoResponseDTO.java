package com.maestria.agenda.financeiro;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ComissaoResponseDTO {
    private Long profissionalId;
    private String nomeProfissional;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private Double comissaoTotal;
    private Double comissaoLiquida;
    private Double comissaoAgendamentosNormais;
    private Double comissaoAgendamentosFixos;
    private Double descontoTaxa; // 🔹 Novo campo para o desconto da taxa

    public ComissaoResponseDTO(Long profissionalId, String nomeProfissional,
                                LocalDate dataInicio, LocalDate dataFim,
                                Double comissaoTotal, Double comissaoLiquida,
                                Double comissaoAgendamentosNormais,
                                Double comissaoAgendamentosFixos,
                                Double descontoTaxa) { // 🔹 Adicionado ao construtor
        this.profissionalId = profissionalId;
        this.nomeProfissional = nomeProfissional;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.comissaoTotal = comissaoTotal;
        this.comissaoLiquida = comissaoLiquida;
        this.comissaoAgendamentosNormais = comissaoAgendamentosNormais;
        this.comissaoAgendamentosFixos = comissaoAgendamentosFixos;
        this.descontoTaxa = descontoTaxa; // 🔹 Atribuindo o novo valor
    }

    // Getters
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

    public Double getComissaoLiquida() {
        return comissaoLiquida;
    }

    public Double getComissaoAgendamentosNormais() {
        return comissaoAgendamentosNormais;
    }

    public Double getComissaoAgendamentosFixos() {
        return comissaoAgendamentosFixos;
    }

    public Double getDescontoTaxa() { // 🔹 Getter para o desconto da taxa
        return descontoTaxa;
    }
}