package com.maestria.agenda.config;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import jakarta.annotation.PostConstruct;

@Configuration
public class DatabaseConnectionChecker {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionChecker.class);
    
    @Value("${spring.datasource.url}")
    private String databaseUrl;
    
    @Value("${spring.datasource.username}")
    private String databaseUsername;
    
    @Value("${spring.datasource.password}")
    private String databasePassword;
    
    @PostConstruct
    public void init() {
        logger.info("Inicializando DatabaseConnectionChecker");
        checkHostReachability();
    }
    
    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationEvent(ContextRefreshedEvent event) {
        logger.info("Aplicação inicializada, verificando conexão com banco de dados");
        checkDatabaseConnection();
    }
    
    private void checkHostReachability() {
        try {
            // Extrai o hostname da URL do banco de dados
            String jdbcUrl = databaseUrl;
            String hostname = jdbcUrl.split("://")[1].split(":")[0];
            
            logger.info("Verificando disponibilidade do host: {}", hostname);
            
            // Tenta resolver o hostname
            InetAddress address = InetAddress.getByName(hostname);
            logger.info("Host resolvido com sucesso: {} -> {}", hostname, address.getHostAddress());
            
            // Verifica se o host responde
            boolean reachable = address.isReachable(5000);
            logger.info("Host está acessível? {}", reachable);
            
        } catch (Exception e) {
            logger.error("Erro ao verificar host do banco de dados: {}", e.getMessage(), e);
            logNetworkInfo();
        }
    }
    
    private void checkDatabaseConnection() {
        try {
            logger.info("Tentando conexão direta com o banco de dados usando JDBC: {}", databaseUrl);
            
            // Tentativa de conexão JDBC direta
            try (Connection conn = DriverManager.getConnection(databaseUrl, databaseUsername, databasePassword)) {
                logger.info("Conexão JDBC estabelecida com sucesso!");
            }
        } catch (Exception e) {
            logger.error("Falha na conexão JDBC: {}", e.getMessage(), e);
            logNetworkInfo();
        }
    }
    
    private void logNetworkInfo() {
        try {
            logger.info("---- Informações de Rede ----");
            
            // Obtém o endereço IP local
            InetAddress localHost = InetAddress.getLocalHost();
            logger.info("Endereço IP local: {}", localHost.getHostAddress());
            logger.info("Nome do host local: {}", localHost.getHostName());
            
            // Verifica conexão com servidores públicos
            logger.info("Teste de conectividade com google.com: {}", 
                    InetAddress.getByName("google.com").isReachable(5000));
            
            logger.info("---- Fim das Informações de Rede ----");
        } catch (Exception e) {
            logger.error("Erro ao obter informações de rede: {}", e.getMessage());
        }
    }
}
