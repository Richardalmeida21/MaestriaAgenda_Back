package com.maestria.agenda.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller para receber webhooks do WhatsApp Cloud API
 * Este controller processa as chamadas de verificação e recebimento de mensagens
 */
@RestController
@RequestMapping("/api/whatsapp/webhook")
public class WhatsAppWebhookController {
    
    private static final Logger logger = LoggerFactory.getLogger(WhatsAppWebhookController.class);
    
    @Value("${whatsapp.webhook.verification-token:whatsapp_mastria_2025_token_seguro_IIrr2931!}")
    private String verificationToken;
    
    /**
     * Endpoint para verificação do webhook pela Meta
     * A Meta chamará este endpoint com um desafio para confirmar que você controla este URL
     */
    @GetMapping
    public ResponseEntity<String> verificarWebhook(
            @RequestParam(value = "hub.mode", required = false) String mode,
            @RequestParam(value = "hub.verify_token", required = false) String token,
            @RequestParam(value = "hub.challenge", required = false) String challenge) {
        
        logger.info("Recebida solicitação de verificação do webhook: mode={}, token={}, challenge={}", mode, token, challenge);
        logger.info("Token configurado: {}", verificationToken);
        
        // Verificar se todos os parâmetros foram recebidos
        if (mode == null || token == null || challenge == null) {
            logger.error("Verificação falhou: parâmetros incompletos. mode={}, token={}, challenge={}", 
                    mode, token != null ? "presente" : "ausente", challenge != null ? "presente" : "ausente");
            return ResponseEntity.badRequest().body("Parâmetros incompletos");
        }
        
        // Verificar se o token recebido corresponde ao nosso token configurado
        if ("subscribe".equals(mode) && verificationToken.equals(token)) {
            logger.info("Verificação de webhook bem-sucedida. Challenge: {}", challenge);
            return ResponseEntity.ok(challenge);
        } else {
            logger.warn("Falha na verificação do webhook: {} {} {}", 
                    "subscribe".equals(mode) ? "Modo correto" : "Modo incorreto: " + mode,
                    verificationToken.equals(token) ? "Token correto" : "Token incorreto",
                    "Challenge: " + challenge);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token de verificação inválido");
        }
    }
    
    /**
     * Endpoint para receber mensagens e atualizações de status
     * A Meta enviará notificações para este endpoint quando eventos ocorrerem
     */
    @PostMapping
    public ResponseEntity<String> receberMensagens(@RequestBody String payload) {
        logger.info("Mensagem recebida via webhook: {}", payload);
        
        // Aqui você deve implementar a lógica para:
        // 1. Processar mensagens recebidas de clientes
        // 2. Atualizar status de mensagens enviadas
        // 3. Lidar com outros tipos de eventos
        
        // IMPORTANTE: Este é apenas um esqueleto básico.
        // Para uma implementação completa, você deve:
        // - Usar uma biblioteca JSON para processar o payload
        // - Implementar handlers para diferentes tipos de mensagens
        // - Salvar as mensagens no banco de dados
        // - Implementar lógica de resposta automática se necessário
        
        // É importante retornar HTTP 200 rapidamente, mesmo que o processamento
        // seja feito de forma assíncrona em segundo plano
        return ResponseEntity.ok("EVENT_RECEIVED");
    }
}
