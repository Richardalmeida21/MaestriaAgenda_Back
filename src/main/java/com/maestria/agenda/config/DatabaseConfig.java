package com.maestria.agenda.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.net.InetAddress;
import java.util.Arrays;

@Configuration
public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    private final Environment environment;

    public DatabaseConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource dataSource(DataSourceProperties properties) {
        logger.info("Configurando DataSource com URL: {}", jdbcUrl);
        
        // Verifica se estamos no ambiente Render
        boolean isRenderEnvironment = isRenderEnvironment();
        logger.info("Ambiente Render detectado: {}", isRenderEnvironment);
        
        if (isRenderEnvironment) {
            // No Render, podemos precisar de configurações especiais
            logger.info("Aplicando configurações otimizadas para o Render");
            
            // Tentar verificar resolução DNS do host do banco de dados
            tryResolveHostname();
        }
        
        // Criar o DataSource com configurações específicas
        HikariDataSource dataSource = properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
        
        // Configurações adicionais do Hikari
        dataSource.setMaximumPoolSize(2); // Reduzir para economizar recursos no Render
        dataSource.setMinimumIdle(1);
        dataSource.setConnectionTimeout(30000);
        dataSource.setIdleTimeout(600000);
        dataSource.setMaxLifetime(1800000);
        
        // No Render, podemos precisar de configurações adicionais de timeout
        if (isRenderEnvironment) {
            dataSource.setConnectionTimeout(60000); // Timeout maior no Render
            dataSource.setValidationTimeout(10000);
            dataSource.setLeakDetectionThreshold(60000);
        }
        
        return dataSource;
    }
    
    private boolean isRenderEnvironment() {
        // Verificar se estamos executando no Render verificando variáveis de ambiente
        return System.getenv("RENDER") != null || 
               System.getenv("IS_RENDER") != null ||
               Arrays.asList(environment.getActiveProfiles()).contains("render");
    }
    
    private void tryResolveHostname() {
        try {
            // Extrair o hostname da URL JDBC
            // Exemplo: jdbc:postgresql://db.kgcajiuuvcgkggbhtudi.supabase.co:5432/postgres
            String hostname = jdbcUrl.split("://")[1].split(":")[0];
            
            logger.info("Tentando resolver hostname: {}", hostname);
            
            // Tentar resolver o hostname para verificar se o DNS está funcionando
            InetAddress address = InetAddress.getByName(hostname);
            
            logger.info("Hostname resolvido com sucesso: {} -> {}", 
                    hostname, address.getHostAddress());
            
            // Verificar se o host responde
            boolean reachable = address.isReachable(5000);
            logger.info("Host está respondendo? {}", reachable);
            
        } catch (Exception e) {
            logger.error("Falha ao resolver hostname do banco de dados: {}", e.getMessage());
            logger.info("Isso pode indicar um problema de DNS ou rede no ambiente Render");
        }
    }
}
