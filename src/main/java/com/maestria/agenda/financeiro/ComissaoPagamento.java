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
    
    @Column(nullable = false)
    private Long profissionalId;
    
    @Column(nullable = false)
    private LocalDate dataPagamento;
    
    @Column(nullable = false)
    private Double valorPago;
    
    @Column(nullable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();
    
    @Column
    private String observacao;
    
    @Column(nullable = false)
    private Boolean paid = true;
    
    @Column(nullable = false)
    private LocalDate periodoInicio;
    
    @Column(nullable = false)
    private LocalDate periodoFim;
    
    // Construtor padrão para JPA
    public ComissaoPagamento() {}
    
    public ComissaoPagamento(Long profissionalId, LocalDate dataPagamento, Double valorPago, String observacao, 
            LocalDate periodoInicio, LocalDate periodoFim) {
        this.profissionalId = profissionalId;
        this.dataPagamento = dataPagamento;
        this.valorPago = valorPago;
        this.observacao = observacao;
        this.paid = true;
        this.periodoInicio = periodoInicio;
        this.periodoFim = periodoFim;
    }
    
    // Getters e setters
    public Long getId() {
        return id;
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
    
    public LocalDateTime getDataCriacao() {
        return dataCriacao;
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
}
