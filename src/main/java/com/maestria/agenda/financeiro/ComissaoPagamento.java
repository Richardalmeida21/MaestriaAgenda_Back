package com.maestria.agenda.financeiro;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "comissoes_pagamentos")
public class ComissaoPagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "profissional_id", nullable = false)
    private Long profissionalId;
    
    @Column(name = "data_pagamento", nullable = false)
    private LocalDate dataPagamento;
    
    @Column(name = "valor_pago", nullable = false)
    private Double valorPago;
    
    @Column(name = "valor_comissao", nullable = false)
    private Double valorComissao;
    
    @Column(name = "observacao")
    private String observacao;
    
    @Column(name = "paid", nullable = false)
    private Boolean paid;
    
    @Column(name = "periodo_inicio", nullable = false)
    private LocalDate periodoInicio;
    
    @Column(name = "periodo_fim", nullable = false)
    private LocalDate periodoFim;
    
    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;
    
    // Construtor padrão para JPA
    public ComissaoPagamento() {
        this.paid = true;
        this.dataCriacao = LocalDateTime.now();
    }
    
    public ComissaoPagamento(Long profissionalId, LocalDate dataPagamento, Double valorPago, String observacao, 
            LocalDate periodoInicio, LocalDate periodoFim) {
        this();
        this.profissionalId = profissionalId;
        this.dataPagamento = dataPagamento;
        this.valorPago = valorPago;
        this.valorComissao = valorPago; // Inicialmente o valor da comissão é igual ao valor pago
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
    
    public LocalDate getDataPagamento() {
        return dataPagamento;
    }
    
    public void setDataPagamento(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
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
}
