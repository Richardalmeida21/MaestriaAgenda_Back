package com.maestria.agenda.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WhatsAppConfig {

    @Value("${whatsapp.cloud.api.version}")
    private String apiVersion;

    @Bean
    public WebClient whatsappApiClient() {
        return WebClient.builder()
                .baseUrl("https://graph.facebook.com/" + apiVersion)
                .build();
    }
}
