package com.maestria.agenda.financeiro;

public enum PagamentoTipo {

    CREDITO_1X(2.0),
    CREDITO_2X(2.5),
    CREDITO_3X(3.0),
    CREDITO_4X(3.5),
    CREDITO_5X(4.0),
    CREDITO_6X(4.5),
    CREDITO_7X(5.0),
    CREDITO_8X(5.0),
    CREDITO_9X(5.0),
    CREDITO_10X(5.0),
    DEBITO(1.5),
    PIX(0.0),
    DINHEIRO(0.0);
    
    private final double taxa;

    PagamentoTipo(double taxa) {
        this.taxa = taxa;
    }

    public double getTaxa() {
        return taxa;
    }
    
    public static PagamentoTipo fromString(String formaPagamento) {
        try {
            return PagamentoTipo.valueOf(formaPagamento.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}