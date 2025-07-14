package com.maestria.agenda.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.util.Arrays;

@Configuration
public class RenderNetworkConfig {
    private static final Logger logger = LoggerFactory.getLogger(RenderNetworkConfig.class);

    private final Environment environment;

    public RenderNetworkConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    @Profile("render")
    public CommandLineRunner checkRenderNetworkSettings() {
        return args -> {
            logger.info("Verificando configurações de rede no ambiente Render...");
            
            // Verificar perfis ativos
            logger.info("Perfis ativos: {}", Arrays.toString(environment.getActiveProfiles()));
            
            // Verificar variáveis de ambiente relacionadas à rede no Render
            logRenderEnvironmentVars();
            
            // Verificar conectividade para endereços comuns
            checkConnectivity("google.com");
            checkConnectivity("db.kgcajiuuvcgkggbhtudi.supabase.co");
            
            logger.info("Verificação de rede no Render concluída.");
        };
    }
    
    private void logRenderEnvironmentVars() {
        logger.info("Variáveis de ambiente do Render:");
        logger.info("RENDER_SERVICE_ID: {}", System.getenv("RENDER_SERVICE_ID"));
        logger.info("RENDER_INSTANCE_ID: {}", System.getenv("RENDER_INSTANCE_ID"));
        logger.info("RENDER_EXTERNAL_URL: {}", System.getenv("RENDER_EXTERNAL_URL"));
    }
    
    private void checkConnectivity(String host) {
        try {
            logger.info("Verificando conectividade para: {}", host);
            
            // Tentativa de resolver o nome do host
            InetAddress address = InetAddress.getByName(host);
            logger.info("Resolução DNS bem-sucedida: {} -> {}", host, address.getHostAddress());
            
            // Verificar se o host responde
            boolean reachable = address.isReachable(5000);
            logger.info("Host {} está respondendo ao ping: {}", host, reachable);
            
        } catch (Exception e) {
            logger.error("Falha ao verificar conectividade para {}: {}", host, e.getMessage());
        }
    }
}
