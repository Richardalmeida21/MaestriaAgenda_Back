package com.maestria.agenda.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@Service
public class WppConnectService {

    private static final Logger logger = LoggerFactory.getLogger(WppConnectService.class);

    private final RestTemplate restTemplate;

    @Value("${wppconnect.server.url}")
    private String wppConnectServerUrl;

    @Value("${wppconnect.session.name}")
    private String sessionName;
    
    @Value("${wppconnect.token}")
    private String token;

    public WppConnectService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean enviarMensagem(String telefone, String mensagem) {
        try {
            String numeroFormatado = formatarTelefone(telefone);
            if (numeroFormatado == null) {
                logger.error("Número de telefone inválido fornecido: {}", telefone);
                return false;
            }

            String url = wppConnectServerUrl + "/api/" + sessionName + "/send-message";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);

            Map<String, Object> body = new HashMap<>();
            body.put("phone", numeroFormatado);
            body.put("message", mensagem);
            body.put("isGroup", false);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            logger.info("Enviando mensagem para WPPConnect: URL={}, Body={}", url, body);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Mensagem enviada com sucesso via WPPConnect para {}. Response: {}", numeroFormatado, response.getBody());
                return true;
            } else {
                logger.error("Falha ao enviar mensagem via WPPConnect para {}. Status: {}, Response: {}", numeroFormatado, response.getStatusCode(), response.getBody());
                return false;
            }
        } catch (Exception e) {
            logger.error("Erro ao enviar mensagem via WPPConnect para {}: {}", telefone, e.getMessage(), e);
            return false;
        }
    }

    private String formatarTelefone(String telefone) {
        if (telefone == null || telefone.trim().isEmpty()) {
            return null;
        }
        String numeroLimpo = telefone.replaceAll("\\D", "");

        if (numeroLimpo.length() < 10) {
             logger.warn("Número de telefone '{}' parece ser muito curto.", telefone);
             return numeroLimpo;
        }

        if (numeroLimpo.length() == 11 && numeroLimpo.startsWith("0")) {
            numeroLimpo = numeroLimpo.substring(1);
        }

        if (!numeroLimpo.startsWith("55")) {
            if (numeroLimpo.length() == 11 || numeroLimpo.length() == 10) {
                 return "55" + numeroLimpo;
            }
        }

        return numeroLimpo;
    }
}
