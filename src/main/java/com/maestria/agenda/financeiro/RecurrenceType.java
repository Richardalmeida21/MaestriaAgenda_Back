package com.maestria.agenda.financeiro;

public enum RecurrenceType {
    DAILY,      // Diário
    WEEKLY,     // Semanal (recurrenceValue é uma máscara de bits para os dias da semana)
    MONTHLY,    // Mensal (recurrenceValue é o dia do mês, -1 para último dia)
    YEARLY      // Anual (recorrenceValue é o dia do ano)
}
