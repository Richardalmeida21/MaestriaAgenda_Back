package com.maestria.agenda.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração de cache para melhorar performance
 * Usa cache em memória (ConcurrentHashMap) - ideal para plano free
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(
                "profissionais", // Cache de profissionais
                "clientes", // Cache de clientes
                "servicos", // Cache de serviços
                "servicosAtivos", // Cache de serviços ativos
                "taxasPagamento" // Cache de taxas de pagamento
        );
        return cacheManager;
    }
}
