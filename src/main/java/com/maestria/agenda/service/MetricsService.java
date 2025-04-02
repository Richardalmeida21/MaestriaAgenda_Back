package com.maestria.agenda.service;

import com.maestria.agenda.agendamento.AgendamentoRepository;
import com.maestria.agenda.financeiro.MetricasGeraisDTO;
import com.maestria.agenda.financeiro.RevenueData;
import com.maestria.agenda.financeiro.ServiceData;
import com.maestria.agenda.financeiro.ClientData;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class MetricsService {

    private final AgendamentoRepository agendamentoRepository;

    public MetricsService(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    public MetricasGeraisDTO obterMetricasGerais(LocalDate dataInicio, LocalDate dataFim) {
        Double totalRevenue = agendamentoRepository.calcularFaturamentoTotalPorPeriodo(dataInicio, dataFim);
        totalRevenue = (totalRevenue != null) ? totalRevenue : 0.0;

        Integer servicesCount = agendamentoRepository.contarServicosRealizadosPorPeriodo(dataInicio, dataFim);
        servicesCount = (servicesCount != null) ? servicesCount : 0;

        Double avgTicket = servicesCount > 0 ? totalRevenue / servicesCount : 0.0;

        Integer newClients = agendamentoRepository.contarNovosClientesPorPeriodo(dataInicio, dataFim);
        newClients = (newClients != null) ? newClients : 0;

        Integer clientsCount = agendamentoRepository.contarTotalDeClientesPorPeriodo(dataInicio, dataFim);
        clientsCount = (clientsCount != null) ? clientsCount : 0;

        Double returnRate = 0.0; // implemente sua regra para a taxa de retorno

        return new MetricasGeraisDTO(totalRevenue, servicesCount, avgTicket, newClients, clientsCount, returnRate);
    }
    
    // Método para obter dados de faturamento mensais
    public List<RevenueData> obterFaturamentoMensal(LocalDate dataInicio, LocalDate dataFim) {
        List<RevenueData> revenueDataList = new ArrayList<>();
        LocalDate current = dataInicio.withDayOfMonth(1);
        int monthsCount = 0;
        while (!current.isAfter(dataFim)) {
            monthsCount++;
            String monthName = current.getMonth().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR"));
            revenueDataList.add(new RevenueData(monthName, 0.0)); // inicializa com 0.0
            current = current.plusMonths(1);
        }
        // Exemplo: dividir o faturamento total igualmente entre os meses
        MetricasGeraisDTO metricas = this.obterMetricasGerais(dataInicio, dataFim);
        if (monthsCount > 0) {
            double revenuePerMonth = metricas.totalRevenue() / monthsCount;
            for (RevenueData rd : revenueDataList) {
                rd = new RevenueData(rd.month(), revenuePerMonth); // ou atualize seu record se for imutável (crie novos records)
            }
            // Se seus records forem imutáveis, recrie a lista:
            List<RevenueData> updatedList = new ArrayList<>();
            for (RevenueData rd : revenueDataList) {
                updatedList.add(new RevenueData(rd.month(), revenuePerMonth));
            }
            revenueDataList = updatedList;
        }
        return revenueDataList;
    }
    
    // Método para obter dados de serviços realizados - aqui você implementa sua lógica real (agrupando por serviço)
    public List<ServiceData> obterDadosDeServicos(LocalDate dataInicio, LocalDate dataFim) {
        List<ServiceData> list = new ArrayList<>();
        // Exemplo fictício:
        list.add(new ServiceData("Corte", 120));
        list.add(new ServiceData("Depilação", 80));
        return list;
    }
    
    // Método para obter dados de clientes (novos x recorrentes) agrupados por mês
    public List<ClientData> obterDadosDeClientes(LocalDate dataInicio, LocalDate dataFim) {
        List<ClientData> list = new ArrayList<>();
        // Exemplo fictício (você deve implementar sua consulta real):
        list.add(new ClientData("Mar", 20, 30));
        list.add(new ClientData("Abr", 15, 25));
        return list;
    }
}