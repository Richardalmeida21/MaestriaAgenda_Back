package com.maestria.agenda.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.maestria.agenda.agendamento.Agendamento;

import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class NotificacaoService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificacaoService.class);
    
    private final WebClient whatsappApiClient;
    private final ObjectMapper objectMapper;
    
    @Value("${whatsapp.cloud.api.token}")
    private String apiToken;
    
    @Value("${whatsapp.cloud.api.phone.number.id}")
    private String phoneNumberId;
    
    @Value("${whatsapp.enabled:false}")
    private boolean whatsappEnabled;
    
    @Value("${whatsapp.cloud.api.version}")
    private String apiVersion;
    
    public NotificacaoService(WebClient whatsappApiClient, ObjectMapper objectMapper) {
        this.whatsappApiClient = whatsappApiClient;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Envia uma mensagem de lembrete de agendamento pelo WhatsApp
     * 
     * @param agendamento O agendamento para o qual enviar o lembrete
     * @return true se a mensagem foi enviada com sucesso, false caso contrário
     */
    public boolean enviarLembreteWhatsApp(Agendamento agendamento) {
        if (!whatsappEnabled) {
            logger.info("WhatsApp API está desativada. Simulando envio de mensagem para: {}", agendamento.getCliente().getTelefone());
            return true;
        }
        
        try {
            String telefoneCliente = formatarNumeroTelefone(agendamento.getCliente().getTelefone());
            if (telefoneCliente == null) {
                logger.error("Número de telefone inválido para cliente id: {}", agendamento.getCliente().getId());
                return false;
            }
            
            String mensagem = criarMensagemLembrete(agendamento);
            
            return enviarMensagemDireta(telefoneCliente, mensagem);
        } catch (Exception e) {
            logger.error("Erro ao enviar mensagem WhatsApp: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Envia uma mensagem diretamente para um número de telefone
     * 
     * @param telefone O número de telefone de destino
     * @param mensagem A mensagem a ser enviada
     * @return true se a mensagem foi enviada com sucesso, false caso contrário
     */
    public boolean enviarMensagemDireta(String telefone, String mensagem) {
        if (!whatsappEnabled) {
            logger.info("WhatsApp API está desativada. Simulando envio de mensagem para: {}", telefone);
            return true;
        }
        
        try {
            String telefoneFormatado = formatarNumeroTelefone(telefone);
            if (telefoneFormatado == null) {
                logger.error("Número de telefone inválido: {}", telefone);
                return false;
            }
            
            // Criar o payload para a API do WhatsApp
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("messaging_product", "whatsapp");
            requestBody.put("recipient_type", "individual");
            requestBody.put("to", telefoneFormatado);
            requestBody.put("type", "text");
            
            ObjectNode textNode = objectMapper.createObjectNode();
            textNode.put("preview_url", false);
            textNode.put("body", mensagem);
            requestBody.set("text", textNode);
            
            // Log do payload para depuração
            logger.info("Enviando mensagem WhatsApp: {}", requestBody.toString());
            
            // Enviar a mensagem usando o WebClient
            String response = whatsappApiClient.post()
                    .uri("/" + phoneNumberId + "/messages")
                    .header("Authorization", "Bearer " + apiToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(requestBody), ObjectNode.class)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            logger.info("Mensagem enviada com sucesso. Resposta: {}", response);
            return true;
        } catch (Exception e) {
            logger.error("Erro ao enviar mensagem WhatsApp: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Verifica se a configuração do WhatsApp está correta
     * 
     * @return Um mapa com informações sobre o status da configuração
     */
    public Map<String, Object> verificarConfiguracao() {
        Map<String, Object> status = new HashMap<>();
        
        // Verificar se o WhatsApp está habilitado
        status.put("whatsappEnabled", whatsappEnabled);
        
        // Verificar se o token de API está configurado
        boolean tokenConfigurado = apiToken != null && !apiToken.isEmpty() && !apiToken.equals("${WHATSAPP_API_TOKEN}");
        status.put("tokenConfigurado", tokenConfigurado);
        
        // Verificar se o ID do número de telefone está configurado
        boolean phoneIdConfigurado = phoneNumberId != null && !phoneNumberId.isEmpty() && !phoneNumberId.equals("${WHATSAPP_PHONE_NUMBER_ID}");
        status.put("phoneIdConfigurado", phoneIdConfigurado);
        
        // Verificar a versão da API
        status.put("apiVersion", apiVersion);
        
        // Status geral da configuração
        status.put("configuracaoCompleta", whatsappEnabled && tokenConfigurado && phoneIdConfigurado);
        
        // Adicionar URL de endpoint de teste
        status.put("endpointTesteWhatsApp", "/notificacoes/teste-whatsapp");
        status.put("formatoTesteWhatsApp", "{\"telefone\": \"5511999999999\", \"mensagem\": \"Teste\"}");
        
        // Próximos passos
        if (!whatsappEnabled) {
            status.put("proximoPasso", "Ativar WhatsApp configurando WHATSAPP_ENABLED=true");
        } else if (!tokenConfigurado) {
            status.put("proximoPasso", "Configurar token de API (WHATSAPP_API_TOKEN)");
        } else if (!phoneIdConfigurado) {
            status.put("proximoPasso", "Configurar ID do número de telefone (WHATSAPP_PHONE_NUMBER_ID)");
        } else {
            status.put("proximoPasso", "Configuração completa! Teste o envio de mensagens com o endpoint /notificacoes/teste-whatsapp");
        }
        
        return status;
    }
    
    /**
     * Formata o número de telefone para o formato aceito pelo WhatsApp
     * Remove caracteres não numéricos e adiciona o código do país se necessário
     */
    private String formatarNumeroTelefone(String telefone) {
        if (telefone == null || telefone.isEmpty()) {
            return null;
        }
        
        // Remove todos os caracteres não numéricos
        String numeroLimpo = telefone.replaceAll("[^0-9]", "");
        
        // Se o número já começar com +, retorna como está
        if (telefone.startsWith("+")) {
            return numeroLimpo;
        }
        
        // Se o número começar com 0, remove o 0
        if (numeroLimpo.startsWith("0")) {
            numeroLimpo = numeroLimpo.substring(1);
        }
        
        // Se o número não começar com 55 (Brasil), adiciona
        if (!numeroLimpo.startsWith("55")) {
            numeroLimpo = "55" + numeroLimpo;
        }
        
        return numeroLimpo;
    }
    
    /**
     * Cria a mensagem de lembrete para o agendamento
     */
    private String criarMensagemLembrete(Agendamento agendamento) {
        return String.format(
                "Olá %s! Este é um lembrete para seu agendamento amanhã (%s) às %s com %s para o serviço de %s. Agradecemos a preferência!",
                agendamento.getCliente().getNome(),
                agendamento.getData().plusDays(1).toString(),
                agendamento.getHora().toString(),
                agendamento.getProfissional().getNome(),
                agendamento.getServico().getNome()
        );
    }
}
