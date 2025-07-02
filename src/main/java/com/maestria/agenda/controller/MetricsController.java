package com.maestria.agenda.controller;

import com.maestria.agenda.financeiro.MetricasGeraisDTO;
import com.maestria.agenda.financeiro.RevenueData;
import com.maestria.agenda.financeiro.ServiceData;
import com.maestria.agenda.financeiro.ClientData;
import com.maestria.agenda.financeiro.HorarioData;
import com.maestria.agenda.service.MetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/metricas")
@CrossOrigin(origins = "*")
public class MetricsController {

    private static final Logger logger = LoggerFactory.getLogger(MetricsController.class);
    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping
    public ResponseEntity<?> obterMetricas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @AuthenticationPrincipal UserDetails userDetails) {

        logger.info("Requesting metrics from {} to {} by user {}", dataInicio, dataFim, userDetails.getUsername());

        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            return ResponseEntity.status(403).body("Access denied. Only ADMIN can access metrics.");
        }

        try {
            MetricasGeraisDTO metricas = metricsService.obterMetricasGerais(dataInicio, dataFim);
            List<RevenueData> revenueData = metricsService.obterFaturamentoMensal(dataInicio, dataFim);
            List<ServiceData> serviceData = metricsService.obterDadosDeServicos(dataInicio, dataFim);
            List<ClientData> clientData = metricsService.obterDadosDeClientes(dataInicio, dataFim);
            List<HorarioData> horarioData = metricsService.obterHorariosMaisProcurados(dataInicio, dataFim);

            Map<String, Object> response = new HashMap<>();
            response.put("totalRevenue", metricas.totalRevenue());
            response.put("servicesCount", metricas.servicesCount());
            response.put("avgTicket", metricas.avgTicket());
            response.put("newClients", metricas.newClients());
            response.put("clientsCount", metricas.clientsCount());
            response.put("returnRate", metricas.returnRate());
            response.put("faturamentoMensal", revenueData); // Mudança de nome para corresponder ao frontend
            response.put("servicosAgendados", serviceData); // Mudança de nome para corresponder ao frontend
            response.put("clientesNovosRecorrentes", clientData); // Mudança de nome para corresponder ao frontend
            response.put("horariosMaisProcurados", horarioData); // Adicionado
            response.put("totalExpenses", metricas.totalExpenses());
            response.put("totalCommissions", metricas.totalCommissions());
            response.put("profit", metricas.profit());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching metrics", e);
            return ResponseEntity.status(500).body("Error fetching metrics: " + e.getMessage());
        }
    }
}