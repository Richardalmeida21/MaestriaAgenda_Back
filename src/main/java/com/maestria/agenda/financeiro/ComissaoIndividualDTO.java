package com.maestria.agenda.financeiro;

import java.time.LocalDate;

public class ComissaoIndividualDTO {
    private Long id;
    private Long agendamentoId;
    private LocalDate dataPagamento;
    private Double valorComissao;
    private String status;
    private Boolean paid;

    public ComissaoIndividualDTO(Long id, Long agendamentoId, LocalDate dataPagamento, 
            Double valorComissao, String status, Boolean paid) {
        this.id = id;
        this.agendamentoId = agendamentoId;
        this.dataPagamento = dataPagamento;
        this.valorComissao = valorComissao;
        this.status = status;
        this.paid = paid;
    }

    public Long getId() {
        return id;
    }

    public Long getAgendamentoId() {
        return agendamentoId;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public Double getValorComissao() {
        return valorComissao;
    }

    public String getStatus() {
        return status;
    }

    public Boolean getPaid() {
        return paid;
    }
} 