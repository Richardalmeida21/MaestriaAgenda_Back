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
    private LocalDate periodoInicio;
    
    @Column(nullable = false)
    private LocalDate periodoFim;
    
    @Column(nullable = false)
    private Double valorComissao;
    
    @Column(nullable = false)
    private Boolean paid = false;
    
    @Column(nullable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();
    
    @Column
    private LocalDateTime dataPagamento;
    
    // Construtor padrão para JPA
    public ComissaoPagamento() {}
    
    public ComissaoPagamento(Long profissionalId, LocalDate periodoInicio, LocalDate periodoFim, 
                            Double valorComissao, Boolean paid) {
        this.profissionalId = profissionalId;
        this.periodoInicio = periodoInicio;
        this.periodoFim = periodoFim;
        this.valorComissao = valorComissao;
        this.paid = paid;
        if (paid) {
            this.dataPagamento = LocalDateTime.now();
        }
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
    
    public Double getValorComissao() {
        return valorComissao;
    }
    
    public void setValorComissao(Double valorComissao) {
        this.valorComissao = valorComissao;
    }
    
    public Boolean getPaid() {
        return paid;
    }
    
    public void setPaid(Boolean paid) {
        this.paid = paid;
        if (paid && dataPagamento == null) {
            this.dataPagamento = LocalDateTime.now();
        } else if (!paid) {
            this.dataPagamento = null;
        }
    }
    
    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }
    
    public LocalDateTime getDataPagamento() {
        return dataPagamento;
    }
    
    public void setDataPagamento(LocalDateTime dataPagamento) {
        this.dataPagamento = dataPagamento;
    }
}
