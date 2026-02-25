package com.maestria.agenda.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuração de cache com Caffeine para melhorar performance
 * Cache expira automaticamente após 5 minutos
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "profissionais",
                "clientes", 
                "servicos",
                "taxasPagamento",
                "metricas",        // Cache para métricas gerais
                "faturamento",     // Cache para faturamento mensal
                "horarios",        // Cache para horários mais procurados
                "clientesData"     // Cache para dados de clientes novos/recorrentes
        );
        
        cacheManager.setCaffeine(Caffeine.newBuilder()
            // Cache expira após 5 minutos (mesmo tempo do React Query)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            // Máximo 200 entradas no cache
            .maximumSize(200)
            // Estatísticas para monitoramento
            .recordStats()
        );
        
        return cacheManager;
    }
}
