package com.maestria.agenda.financeiro;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ComissaoIndividualDTO {
    private Long id;
    private Long agendamentoId;
    private LocalDate dataPagamento;
    private LocalDateTime dataHoraPagamento;
    private Double valorComissao;
    private String status;
    private Boolean paid;

    public ComissaoIndividualDTO(Long id, Long agendamentoId, LocalDate dataPagamento, 
            LocalDateTime dataHoraPagamento, Double valorComissao, String status, Boolean paid) {
        this.id = id;
        this.agendamentoId = agendamentoId;
        this.dataPagamento = dataPagamento;
        this.dataHoraPagamento = dataHoraPagamento;
        this.valorComissao = valorComissao;
        this.status = status;
        this.paid = paid;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAgendamentoId() {
        return agendamentoId;
    }

    public void setAgendamentoId(Long agendamentoId) {
        this.agendamentoId = agendamentoId;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public LocalDateTime getDataHoraPagamento() {
        return dataHoraPagamento;
    }

    public void setDataHoraPagamento(LocalDateTime dataHoraPagamento) {
        this.dataHoraPagamento = dataHoraPagamento;
    }

    public Double getValorComissao() {
        return valorComissao;
    }

    public void setValorComissao(Double valorComissao) {
        this.valorComissao = valorComissao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getPaid() {
        return paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }
} 