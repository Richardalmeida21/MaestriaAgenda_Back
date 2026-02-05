package com.maestria.agenda.financeiro;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ComissaoResponseDTO {
    private Long profissionalId;
    private String nomeProfissional;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private Double comissaoTotal; // Valor bruto (sem desconto)
    private Double comissaoComTaxa; // Valor com desconto de taxa aplicado
    private Double comissaoSemTaxa; // Valor sem desconto de taxa (= comissaoTotal)
    private Double comissaoLiquida; // Valor final baseado na configuração do profissional
    private Double valorTotalNormais;
    private Double valorTotalFixos;
    private Double valorTotalGeral; // Soma de normais + fixos

    private Double comissaoAgendamentosNormais;
    private Double comissaoAgendamentosFixos;
    private Double descontoTaxa;
    private Double valorPago;
    private Double valorPendente;
    private List<ComissaoIndividualDTO> comissoesIndividuais;
    private List<ComissaoPagamento> historicoPagamentos;

    @JsonIgnore
    private boolean pendente;

    public ComissaoResponseDTO(Long profissionalId, String nomeProfissional,
            LocalDate dataInicio, LocalDate dataFim,
            Double comissaoTotal, Double comissaoLiquida,
            Double comissaoAgendamentosNormais,
            Double comissaoAgendamentosFixos,
            Double valorTotalNormais,
            Double valorTotalFixos,
            Double descontoTaxa,
            Double valorPago,
            List<ComissaoIndividualDTO> comissoesIndividuais,
            List<ComissaoPagamento> historicoPagamentos) {
        this.profissionalId = profissionalId;
        this.nomeProfissional = nomeProfissional;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.comissaoTotal = comissaoTotal;
        this.comissaoSemTaxa = comissaoTotal;
        this.comissaoComTaxa = comissaoTotal - descontoTaxa;
        this.comissaoLiquida = comissaoLiquida;
        this.comissaoAgendamentosNormais = comissaoAgendamentosNormais;
        this.comissaoAgendamentosFixos = comissaoAgendamentosFixos;
        this.valorTotalNormais = valorTotalNormais;
        this.valorTotalFixos = valorTotalFixos;
        this.valorTotalGeral = (valorTotalNormais != null ? valorTotalNormais : 0.0) +
                (valorTotalFixos != null ? valorTotalFixos : 0.0);
        this.descontoTaxa = descontoTaxa;
        this.valorPago = valorPago;
        this.valorPendente = Math.max(0, comissaoLiquida - valorPago);
        this.pendente = valorPendente > 0;
        this.comissoesIndividuais = comissoesIndividuais;
        this.historicoPagamentos = historicoPagamentos;
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

    public Double getComissaoSemTaxa() {
        return comissaoSemTaxa;
    }

    public Double getComissaoComTaxa() {
        return comissaoComTaxa;
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

    public Double getValorTotalNormais() {
        return valorTotalNormais;
    }

    public Double getValorTotalFixos() {
        return valorTotalFixos;
    }

    public Double getValorTotalGeral() {
        return valorTotalGeral;
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

    public List<ComissaoIndividualDTO> getComissoesIndividuais() {
        return comissoesIndividuais;
    }

    public List<ComissaoPagamento> getHistoricoPagamentos() {
        return historicoPagamentos;
    }

    public void setHistoricoPagamentos(List<ComissaoPagamento> historicoPagamentos) {
        this.historicoPagamentos = historicoPagamentos;
    }
}