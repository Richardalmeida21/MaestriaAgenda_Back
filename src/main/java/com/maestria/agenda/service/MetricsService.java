package com.maestria.agenda.service;

import com.maestria.agenda.agendamento.AgendamentoRepository;
import com.maestria.agenda.financeiro.MetricasGeraisDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class MetricsService {

    private final AgendamentoRepository agendamentoRepository;

    public MetricsService(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    public MetricasGeraisDTO obterMetricasGerais(LocalDate dataInicio, LocalDate dataFim) {
        // Buscar faturamento total
        Double totalRevenue = agendamentoRepository.calcularFaturamentoTotalPorPeriodo(dataInicio, dataFim);
        totalRevenue = (totalRevenue != null) ? totalRevenue : 0.0;

        // Contar os agendamentos realizados (serviços realizados)
        Integer servicesCount = agendamentoRepository.contarServicosRealizadosPorPeriodo(dataInicio, dataFim);
        servicesCount = (servicesCount != null) ? servicesCount : 0;

        // Calcular o ticket médio
        Double avgTicket = servicesCount > 0 ? totalRevenue / servicesCount : 0.0;

        // Número de novos clientes (assumindo que Cliente possui dataCadastro)
        Integer newClients = agendamentoRepository.contarNovosClientesPorPeriodo(dataInicio, dataFim);
        newClients = (newClients != null) ? newClients : 0;

        // Número total de clientes atendidos
        Integer clientsCount = agendamentoRepository.contarTotalDeClientesPorPeriodo(dataInicio, dataFim);
        clientsCount = (clientsCount != null) ? clientsCount : 0;

        // Taxa de retorno – implemente a lógica de negócio desejada; aqui, configuramos como 0.0
        Double returnRate = 0.0;

        return new MetricasGeraisDTO(totalRevenue, servicesCount, avgTicket, newClients, clientsCount, returnRate);
    }
}