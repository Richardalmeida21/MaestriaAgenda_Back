package com.maestria.agenda.financeiro;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "taxa_pagamento")
public class TaxaPagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    @NotNull(message = "Tipo de pagamento não pode ser nulo")
    private PagamentoTipo tipoPagamento;

    @Column(nullable = false)
    @NotNull(message = "Taxa não pode ser nula")
    @PositiveOrZero(message = "Taxa deve ser positiva ou zero")
    private Double taxa;

    @Column(nullable = false)
    private Boolean ativo = true;

    // Construtor padrão
    public TaxaPagamento() {}

    // Construtor com parâmetros
    public TaxaPagamento(PagamentoTipo tipoPagamento, Double taxa) {
        this.tipoPagamento = tipoPagamento;
        this.taxa = taxa;
        this.ativo = true;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PagamentoTipo getTipoPagamento() {
        return tipoPagamento;
    }

    public void setTipoPagamento(PagamentoTipo tipoPagamento) {
        this.tipoPagamento = tipoPagamento;
    }

    public Double getTaxa() {
        return taxa;
    }

    public void setTaxa(Double taxa) {
        this.taxa = taxa;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    @Override
    public String toString() {
        return "TaxaPagamento{" +
                "id=" + id +
                ", tipoPagamento=" + tipoPagamento +
                ", taxa=" + taxa +
                ", ativo=" + ativo +
                '}';
    }
}
