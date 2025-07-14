package com.maestria.agenda.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DatabaseConnectionInitializer {
    
    private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionInitializer.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @PostConstruct
    public void initializeDatabase() {
        int maxRetries = 5;
        int retryCount = 0;
        boolean connected = false;
        
        while (!connected && retryCount < maxRetries) {
            try {
                // Tente fazer uma consulta simples
                jdbcTemplate.queryForObject("SELECT 1", Integer.class);
                connected = true;
                log.info("Conexão com o banco de dados estabelecida com sucesso");
            } catch (Exception e) {
                retryCount++;
                log.error("Falha na conexão com o banco de dados, tentativa {} de {}. Erro: {}", 
                          retryCount, maxRetries, e.getMessage());
                try {
                    // Aguarde antes de tentar novamente
                    Thread.sleep(5000); // 5 segundos
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        if (!connected) {
            log.error("Não foi possível estabelecer conexão com o banco de dados após {} tentativas", maxRetries);
        }
    }
}
