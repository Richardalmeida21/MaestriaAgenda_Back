package com.maestria.agenda.financeiro;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private Double descontoTaxa;
    private Double valorPago;
    private Double valorPendente;
    
    @JsonIgnore
    private boolean pendente;

    public ComissaoResponseDTO(Long profissionalId, String nomeProfissional,
                               LocalDate dataInicio, LocalDate dataFim,
                               Double comissaoTotal, Double comissaoLiquida,
                               Double comissaoAgendamentosNormais,
                               Double comissaoAgendamentosFixos,
                               Double descontoTaxa,
                               Double valorPago) {
        this.profissionalId = profissionalId;
        this.nomeProfissional = nomeProfissional;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.comissaoTotal = comissaoTotal;
        this.comissaoLiquida = comissaoLiquida;
        this.comissaoAgendamentosNormais = comissaoAgendamentosNormais;
        this.comissaoAgendamentosFixos = comissaoAgendamentosFixos;
        this.descontoTaxa = descontoTaxa;
        this.valorPago = valorPago;
        this.valorPendente = Math.max(0, comissaoLiquida - valorPago);
        this.pendente = valorPendente > 0;
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

    public Double getDescontoTaxa() {
        return descontoTaxa;
    }
    
    public Double getValorPago() {
        return valorPago;
    }
    
    public Double getValorPendente() {
        return valorPendente;
    }
    
    public boolean isPendente() {
        return pendente;
    }
    
    @JsonIgnore
    public void setPendente(boolean pendente) {
        this.pendente = pendente;
    }
}