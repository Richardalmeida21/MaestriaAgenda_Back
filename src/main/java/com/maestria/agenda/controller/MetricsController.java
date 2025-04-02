package com.maestria.agenda.controller;

import com.maestria.agenda.financeiro.MetricasGeraisDTO;
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

        // Exemplo: somente ADMIN pode acessar essas métricas
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            return ResponseEntity.status(403).body("Access denied. Only ADMIN can access metrics.");
        }

        try {
            MetricasGeraisDTO metricas = metricsService.obterMetricasGerais(dataInicio, dataFim);
            return ResponseEntity.ok(metricas);
        } catch (Exception e) {
            logger.error("Error fetching metrics", e);
            return ResponseEntity.status(500).body("Error fetching metrics: " + e.getMessage());
        }
    }
}