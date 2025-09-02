package com.maestria.agenda.financeiro;

public enum PagamentoTipo {

    CREDITO_1X("Crédito 1x"),
    CREDITO_2X("Crédito 2x"),
    CREDITO_3X("Crédito 3x"),
    CREDITO_4X("Crédito 4x"),
    CREDITO_5X("Crédito 5x"),
    CREDITO_6X("Crédito 6x"),
    CREDITO_7X("Crédito 7x"),
    CREDITO_8X("Crédito 8x"),
    CREDITO_9X("Crédito 9x"),
    CREDITO_10X("Crédito 10x"),
    DEBITO("Débito"),
    PIX("PIX"),
    DINHEIRO("Dinheiro");
    
    private final String descricao;

    PagamentoTipo(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
    
    public static PagamentoTipo fromString(String formaPagamento) {
        try {
            return PagamentoTipo.valueOf(formaPagamento.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}