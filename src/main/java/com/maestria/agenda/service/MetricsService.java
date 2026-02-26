package com.maestria.agenda.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.maestria.agenda.agendamento.Agendamento;
import com.maestria.agenda.agendamento.AgendamentoRepository;
import com.maestria.agenda.financeiro.ClientData;
import com.maestria.agenda.financeiro.ComissaoPagamentoRepository;
import com.maestria.agenda.financeiro.ExpenseRepository;
import com.maestria.agenda.financeiro.HorarioData;
import com.maestria.agenda.financeiro.MetricasGeraisDTO;
import com.maestria.agenda.financeiro.RevenueData;
import com.maestria.agenda.financeiro.ServiceData;

@Service
public class MetricsService {

    private final AgendamentoRepository agendamentoRepository;
    private final ExpenseRepository expenseRepository;
    private final ComissaoPagamentoRepository comissaoPagamentoRepository;

    public MetricsService(AgendamentoRepository agendamentoRepository, ExpenseRepository expenseRepository, ComissaoPagamentoRepository comissaoPagamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.expenseRepository = expenseRepository;
        this.comissaoPagamentoRepository = comissaoPagamentoRepository;
    }

    @Cacheable(value = "metricas", key = "#dataInicio + '-' + #dataFim")
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

        Double returnRate = calcularTaxaRetorno(dataInicio, dataFim);

        // Novos cálculos
        Double totalExpenses = expenseRepository.calcularTotalDespesasPagas(dataInicio, dataFim);
        totalExpenses = (totalExpenses != null) ? totalExpenses : 0.0;

        Double totalCommissions = comissaoPagamentoRepository.calcularValorTotalPagoTodosProfissionaisNoPeriodo(dataInicio, dataFim);
        totalCommissions = (totalCommissions != null) ? totalCommissions : 0.0;

        Double profit = totalRevenue - totalExpenses - totalCommissions;

        return new MetricasGeraisDTO(
            totalRevenue, servicesCount, avgTicket, newClients, clientsCount, returnRate,
            totalExpenses, totalCommissions, profit
        );
    }
    
    // Método para obter faturamento mensal
    @Cacheable(value = "faturamento", key = "#dataInicio + '-' + #dataFim")
    public List<RevenueData> obterFaturamentoMensal(LocalDate dataInicio, LocalDate dataFim) {
        List<RevenueData> revenueDataList = new ArrayList<>();
        
        // Consulta que agrupa o faturamento por mês
        List<Object[]> queryResult = agendamentoRepository.groupRevenueByMonth(dataInicio, dataFim);
        Map<Integer, Double> revenueMap = new HashMap<>();
        for (Object[] row : queryResult) {
            Integer monthNumber = ((Number) row[0]).intValue();
            Double revenue = (Double) row[1];
            revenueMap.put(monthNumber, revenue);
        }
        
        // Itera sobre cada mês do período para preencher os dados, mesmo que haja meses sem faturamento
        LocalDate current = dataInicio.withDayOfMonth(1);
        while (!current.isAfter(dataFim)) {
            int monthNumber = current.getMonthValue();
            String monthName = current.getMonth().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR"));
            Double revenue = revenueMap.getOrDefault(monthNumber, 0.0);
            revenueDataList.add(new RevenueData(monthName, revenue));
            current = current.plusMonths(1);
        }
        return revenueDataList;
    }
    
    // Método para obter dados de serviços realizados - agrupando por serviço a partir dos agendamentos
    @Cacheable(value = "servicos", key = "#dataInicio + '-' + #dataFim")
    public List<ServiceData> obterDadosDeServicos(LocalDate dataInicio, LocalDate dataFim) {
        List<ServiceData> list = new ArrayList<>();
        List<Object[]> resultados = agendamentoRepository.findServicosMaisAgendados(dataInicio, dataFim);
        for (Object[] row : resultados) {
            String servicoNome = (String) row[0];
            Long count = (Long) row[1];
            list.add(new ServiceData(servicoNome, count.intValue()));
        }
        return list;
    }
    
    // Método para calcular a porcentagem de clientes que retornam dentro de 30 dias
    public double calcularTaxaRetorno(LocalDate dataInicio, LocalDate dataFim) {
        List<Object[]> agendamentos = agendamentoRepository.findClientIdsAndDatesBetween(dataInicio, dataFim);
        Map<Long, List<LocalDate>> agendamentosPorCliente = new HashMap<>();
        
        for (Object[] row : agendamentos) {
            Long clienteId = (Long) row[0];
            LocalDate data = (LocalDate) row[1];
            agendamentosPorCliente.computeIfAbsent(clienteId, k -> new ArrayList<>()).add(data);
        }

        int totalClientes = agendamentosPorCliente.size();
        int clientesRetornaram = 0;
        for (List<LocalDate> datas : agendamentosPorCliente.values()) {
            if (datas.size() < 2) continue;
            Collections.sort(datas);
            boolean retornou = false;
            for (int i = 1; i < datas.size(); i++) {
                long diffDays = ChronoUnit.DAYS.between(datas.get(i - 1), datas.get(i));
                if (diffDays <= 30) {
                    retornou = true;
                    break;
                }
            }
            if (retornou) {
                clientesRetornaram++;
            }
        }
        return totalClientes > 0 ? (clientesRetornaram / (double) totalClientes) * 100.0 : 0.0;
    }
    
    // Método para obter os horários mais procurados a partir dos agendamentos
    @Cacheable(value = "horarios", key = "#dataInicio + '-' + #dataFim")
    public List<HorarioData> obterHorariosMaisProcurados(LocalDate dataInicio, LocalDate dataFim) {
        List<HorarioData> horarios = new ArrayList<>();
        List<Object[]> queryResult = agendamentoRepository.findHorariosMaisProcurados(dataInicio, dataFim);
        // Total de agendamentos no período (para cálculo de porcentagem)
        long totalAgendamentos = agendamentoRepository.countByDataBetween(dataInicio, dataFim);
        for (Object[] row : queryResult) {
            Integer hour = ((Number) row[0]).intValue();
            Long count = (Long) row[1];
            int percentage = totalAgendamentos > 0 ? (int) ((count * 100) / totalAgendamentos) : 0;
            horarios.add(new HorarioData(hour, count.intValue(), percentage));
        }
        return horarios;
    }
    
    // MÉTODO PARA OBTER "CLIENTES NOVOS VS. RECORRENTES"
    // Para cada mês, definimos clientes novos como aqueles cuja primeira data de agendamento é naquele mês; os demais são recorrentes.
    @Cacheable(value = "clientesData", key = "#dataInicio + '-' + #dataFim")
    public List<ClientData> obterDadosDeClientes(LocalDate dataInicio, LocalDate dataFim) {
        // Recupera dados primitivos de agendamento (muito mais rápido)
        List<Object[]> agendamentos = agendamentoRepository.findClientIdsAndDatesBetween(dataInicio, dataFim);
        // Mapear a primeira data de agendamento para cada cliente
        Map<Long, LocalDate> primeiraData = new HashMap<>();
        for (Object[] row : agendamentos) {
            Long clientId = (Long) row[0];
            LocalDate data = (LocalDate) row[1];
            if (!primeiraData.containsKey(clientId) || data.isBefore(primeiraData.get(clientId))) {
                primeiraData.put(clientId, data);
            }
        }
        
        // Agrupar os clientes por mês (usaremos o padrão "yyyy-MM" para distinguir ano e mês)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, Set<Long>> clientesPorMes = new HashMap<>();
        for (Object[] row : agendamentos) {
            Long clientId = (Long) row[0];
            LocalDate data = (LocalDate) row[1];
            String mesKey = data.format(formatter);
            clientesPorMes.computeIfAbsent(mesKey, k -> new HashSet<>()).add(clientId);
        }
        
        // Itera sobre cada mês do período para calcular os clientes novos vs. recorrentes
        List<ClientData> lista = new ArrayList<>();
        LocalDate current = dataInicio.withDayOfMonth(1);
        while(!current.isAfter(dataFim)) {
            String mesKey = current.format(formatter);
            Set<Long> clientes = clientesPorMes.getOrDefault(mesKey, Collections.emptySet());
            int novos = 0;
            for (Long clientId : clientes) {
                LocalDate primeira = primeiraData.get(clientId);
                // Se a primeira data de agendamento ocorrer no mesmo mês/ano, o cliente é novo
                if (primeira != null && primeira.format(formatter).equals(mesKey)) {
                    novos++;
                }
            }
            int totalClientes = clientes.size();
            int recorrentes = totalClientes - novos;
            // Rótulo do mês (ex.: "Jan 2024")
            String rotuloMes = current.getMonth().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR")) + " " + current.getYear();
            lista.add(new ClientData(rotuloMes, novos, recorrentes));
            current = current.plusMonths(1);
        }
        return lista;
    }
}