package com.maestria.agenda.financeiro;

import java.time.LocalDate;

public record MetricasGeraisDTO(
    Double totalRevenue,
    Integer servicesCount,
    Double avgTicket,
    Integer newClients,
    Integer clientsCount,
    Double returnRate,
    Double totalExpenses,
    Double totalCommissions,
    Double profit
) {}