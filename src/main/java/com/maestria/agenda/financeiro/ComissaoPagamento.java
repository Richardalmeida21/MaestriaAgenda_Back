package com.maestria.agenda.financeiro;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "comissoes_pagamentos")
public class ComissaoPagamento {

    public enum StatusPagamento {
        PAGO,
        CANCELADO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "profissional_id", nullable = false)
    private Long profissionalId;
    
    @Column(name = "agendamento_id", nullable = true)
    private Long agendamentoId;
    
    @Column(name = "data_pagamento", nullable = false)
    private LocalDate dataPagamento;
    
    @Column(name = "data_hora_pagamento", nullable = false)
    private LocalDateTime dataHoraPagamento;
    
    @Column(name = "valor_pago", nullable = false)
    private Double valorPago;
    
    @Column(name = "valor_comissao", nullable = false)
    private Double valorComissao;
    
    @Column(name = "observacao")
    private String observacao;
    
    @Column(name = "paid", nullable = false)
    private Boolean paid;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusPagamento status;
    
    @Column(name = "periodo_inicio", nullable = false)
    private LocalDate periodoInicio;
    
    @Column(name = "periodo_fim", nullable = false)
    private LocalDate periodoFim;
    
    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;
    
    // Construtor padrão para JPA
    public ComissaoPagamento() {
        this.paid = true;
        this.status = StatusPagamento.PAGO;
        ZoneId zonaSaoPaulo = ZoneId.of("America/Sao_Paulo");
        LocalDateTime dataHoraSaoPaulo = LocalDateTime.now(zonaSaoPaulo);
        this.dataCriacao = dataHoraSaoPaulo;
        this.dataHoraPagamento = dataHoraSaoPaulo;
    }
    
    public ComissaoPagamento(Long profissionalId, Long agendamentoId, LocalDate dataPagamento, Double valorPago, String observacao, 
            LocalDate periodoInicio, LocalDate periodoFim) {
        this();
        this.profissionalId = profissionalId;
        this.agendamentoId = agendamentoId;
        this.dataPagamento = dataPagamento;
        this.valorPago = valorPago;
        this.valorComissao = valorPago;
        this.observacao = observacao;
        this.periodoInicio = periodoInicio;
        this.periodoFim = periodoFim;
    }
    
    // Getters e setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getProfissionalId() {
        return profissionalId;
    }
    
    public void setProfissionalId(Long profissionalId) {
        this.profissionalId = profissionalId;
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
    
    public Double getValorPago() {
        return valorPago;
    }
    
    public void setValorPago(Double valorPago) {
        this.valorPago = valorPago;
    }
    
    public Double getValorComissao() {
        return valorComissao;
    }
    
    public void setValorComissao(Double valorComissao) {
        this.valorComissao = valorComissao;
    }
    
    public String getObservacao() {
        return observacao;
    }
    
    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
    
    public Boolean getPaid() {
        return paid;
    }
    
    public void setPaid(Boolean paid) {
        this.paid = paid;
    }
    
    public LocalDate getPeriodoInicio() {
        return periodoInicio;
    }
    
    public void setPeriodoInicio(LocalDate periodoInicio) {
        this.periodoInicio = periodoInicio;
    }
    
    public LocalDate getPeriodoFim() {
        return periodoFim;
    }
    
    public void setPeriodoFim(LocalDate periodoFim) {
        this.periodoFim = periodoFim;
    }
    
    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }
    
    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
    
    public StatusPagamento getStatus() {
        return status;
    }
    
    public void setStatus(StatusPagamento status) {
        this.status = status;
        this.paid = status == StatusPagamento.PAGO;
    }

    public void cancelarParcialmente(Double valorACancelar) {
        if (valorACancelar == null || valorACancelar <= 0) {
            throw new IllegalArgumentException("O valor a cancelar deve ser maior que zero");
        }
        if (valorACancelar >= this.valorPago) {
            throw new IllegalArgumentException("O valor a cancelar deve ser menor que o valor atual");
        }
        
        // Calcula o novo valor após o cancelamento parcial
        Double novoValor = this.valorPago - valorACancelar;
        
        // Atualiza os valores
        this.valorPago = novoValor;
        this.valorComissao = novoValor;
        
        // Se o valor restante for zero, marca como cancelado
        if (novoValor == 0) {
            this.status = StatusPagamento.CANCELADO;
            this.paid = false;
        }
    }

    public void cancelarComissao() {
        if (this.status == StatusPagamento.CANCELADO) {
            throw new IllegalStateException("Esta comissão já está cancelada");
        }
        this.status = StatusPagamento.CANCELADO;
        this.paid = false;
        this.valorPago = 0.0;
        this.valorComissao = 0.0;
    }

    public void reativarComissao() {
        if (this.status == StatusPagamento.PAGO) {
            throw new IllegalStateException("Esta comissão já está paga");
        }
        this.status = StatusPagamento.PAGO;
        this.paid = true;
    }

    public boolean pertenceAoPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return !this.dataPagamento.isBefore(dataInicio) && !this.dataPagamento.isAfter(dataFim);
    }

    public Double getValorEfetivo() {
        return this.status == StatusPagamento.PAGO ? this.valorComissao : 0.0;
    }
}
