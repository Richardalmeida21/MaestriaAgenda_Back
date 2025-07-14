package com.maestria.agenda.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class WhatsAppService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppService.class);
    
    @Value("${whatsapp.cloud.api.token}")
    private String apiToken;
    
    @Value("${whatsapp.cloud.api.phone.number.id}")
    private String phoneNumberId;
    
    @Value("${whatsapp.cloud.api.version:v18.0}")
    private String apiVersion;
    
    @Value("${whatsapp.enabled:false}")
    private boolean whatsappEnabled;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    private static final String TEMPLATE_LEMBRETE_AGENDAMENTO = "lembrete_agendamento";
    
    /**
     * Envia um lembrete de agendamento via WhatsApp usando o template configurado
     * 
     * @param destinatario O número do telefone do destinatário no formato internacional (ex: 5511999999999)
     * @param nome Nome do cliente
     * @param data Data e hora do agendamento
     * @param servico Nome do serviço agendado
     * @param profissional Nome do profissional
     * @return true se a mensagem foi enviada com sucesso, false caso contrário
     */
    public boolean enviarLembreteAgendamento(String destinatario, String nome, LocalDateTime dataHora, 
                                             String servico, String profissional) {
        if (!whatsappEnabled) {
            logger.info("WhatsApp está desabilitado. Lembrete não enviado para {}", destinatario);
            return false;
        }
        
        try {
            // Formatar a data para o formato DD/MM/YYYY HH:MM
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String dataFormatada = dataHora.format(formatter);
            
            // Normalizar o número de telefone (remover espaços, traços, etc)
            String telefoneNormalizado = normalizarTelefone(destinatario);
            
            // Montar o payload para a API do WhatsApp
            Map<String, Object> request = new HashMap<>();
            Map<String, Object> template = new HashMap<>();
            Map<String, Object> language = new HashMap<>();
            
            language.put("code", "pt_BR");
            template.put("name", TEMPLATE_LEMBRETE_AGENDAMENTO);
            template.put("language", language);
            
            // Adicionar os componentes do template (variáveis)
            Map<String, Object> componentsMap = new HashMap<>();
            componentsMap.put("type", "body");
            
            // Configurar os parâmetros
            Map<String, Object> param1 = new HashMap<>();
            param1.put("type", "text");
            param1.put("text", nome);
            
            Map<String, Object> param2 = new HashMap<>();
            param2.put("type", "text");
            param2.put("text", dataFormatada);
            
            Map<String, Object> param3 = new HashMap<>();
            param3.put("type", "text");
            param3.put("text", servico);
            
            Map<String, Object> param4 = new HashMap<>();
            param4.put("type", "text");
            param4.put("text", profissional);
            
            Map<String, Object>[] parameters = new Map[4];
            parameters[0] = param1;
            parameters[1] = param2;
            parameters[2] = param3;
            parameters[3] = param4;
            
            componentsMap.put("parameters", parameters);
            
            Map<String, Object>[] components = new Map[1];
            components[0] = componentsMap;
            
            template.put("components", components);
            
            request.put("messaging_product", "whatsapp");
            request.put("to", telefoneNormalizado);
            request.put("type", "template");
            request.put("template", template);
            
            // Configurar cabeçalhos
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiToken);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            // URL da API do WhatsApp
            String url = "https://graph.facebook.com/" + apiVersion + "/" + phoneNumberId + "/messages";
            
            // Fazer a requisição para a API
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            
            logger.info("Lembrete de agendamento enviado com sucesso para {} ({}). Resposta: {}", 
                    nome, telefoneNormalizado, response);
            
            return true;
        } catch (Exception e) {
            logger.error("Erro ao enviar lembrete de agendamento via WhatsApp para {}: {}", 
                    destinatario, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Método simplificado para testar o envio de mensagem com o template lembrete_agendamento
     * Útil para testes diretos sem precisar formatar a data
     * 
     * @param telefone Número do telefone do destinatário
     * @param nome Nome do cliente
     * @param dataString Data já formatada (ex: "15/07/2025 14:30")
     * @param servico Nome do serviço
     * @param profissional Nome do profissional
     * @return true se a mensagem foi enviada com sucesso, false caso contrário
     */
    public boolean testarLembreteAgendamento(String telefone, String nome, String dataString, 
                                            String servico, String profissional) {
        if (!whatsappEnabled) {
            logger.info("WhatsApp está desabilitado. Teste de lembrete não enviado para {}", telefone);
            return false;
        }
        
        try {
            // Normalizar o número de telefone (remover espaços, traços, etc)
            String telefoneNormalizado = normalizarTelefone(telefone);
            
            logger.info("Enviando teste de lembrete para {} ({}): Data: {}, Serviço: {}, Profissional: {}", 
                    nome, telefoneNormalizado, dataString, servico, profissional);
            
            // Montar o payload para a API do WhatsApp
            Map<String, Object> request = new HashMap<>();
            Map<String, Object> template = new HashMap<>();
            Map<String, Object> language = new HashMap<>();
            
            language.put("code", "pt_BR");
            template.put("name", TEMPLATE_LEMBRETE_AGENDAMENTO);
            template.put("language", language);
            
            // Adicionar os componentes do template (variáveis)
            Map<String, Object> componentsMap = new HashMap<>();
            componentsMap.put("type", "body");
            
            // Configurar os parâmetros
            Map<String, Object> param1 = new HashMap<>();
            param1.put("type", "text");
            param1.put("text", nome);
            
            Map<String, Object> param2 = new HashMap<>();
            param2.put("type", "text");
            param2.put("text", dataString);
            
            Map<String, Object> param3 = new HashMap<>();
            param3.put("type", "text");
            param3.put("text", servico);
            
            Map<String, Object> param4 = new HashMap<>();
            param4.put("type", "text");
            param4.put("text", profissional);
            
            Map<String, Object>[] parameters = new Map[4];
            parameters[0] = param1;
            parameters[1] = param2;
            parameters[2] = param3;
            parameters[3] = param4;
            
            componentsMap.put("parameters", parameters);
            
            Map<String, Object>[] components = new Map[1];
            components[0] = componentsMap;
            
            template.put("components", components);
            
            request.put("messaging_product", "whatsapp");
            request.put("to", telefoneNormalizado);
            request.put("type", "template");
            request.put("template", template);
            
            // Configurar cabeçalhos
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiToken);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            // URL da API do WhatsApp
            String url = "https://graph.facebook.com/" + apiVersion + "/" + phoneNumberId + "/messages";
            
            // Log do payload para debug
            logger.debug("Payload de teste: {}", request);
            
            // Fazer a requisição para a API
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            
            logger.info("Teste de lembrete enviado com sucesso para {} ({}). Resposta: {}", 
                    nome, telefoneNormalizado, response);
            
            return true;
        } catch (Exception e) {
            logger.error("Erro ao enviar teste de lembrete via WhatsApp para {}: {}", 
                    telefone, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Método simplificado para testar o envio de mensagem com o template hello_world
     * Útil para verificar se a integração básica com a API do WhatsApp está funcionando
     * 
     * @param telefone Número do telefone do destinatário
     * @return true se a mensagem foi enviada com sucesso, false caso contrário
     */
    public boolean testarHelloWorld(String telefone) {
        if (!whatsappEnabled) {
            logger.info("WhatsApp está desabilitado. Teste hello_world não enviado para {}", telefone);
            return false;
        }
        
        try {
            // Normalizar o número de telefone
            String telefoneNormalizado = normalizarTelefone(telefone);
            
            logger.info("Enviando teste hello_world para {}", telefoneNormalizado);
            
            // Montar o payload simples para o template hello_world
            Map<String, Object> request = new HashMap<>();
            Map<String, Object> template = new HashMap<>();
            Map<String, Object> language = new HashMap<>();
            
            language.put("code", "en_US");
            template.put("name", "hello_world");
            template.put("language", language);
            
            request.put("messaging_product", "whatsapp");
            request.put("to", telefoneNormalizado);
            request.put("type", "template");
            request.put("template", template);
            
            // Configurar cabeçalhos
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiToken);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            // URL da API do WhatsApp
            String url = "https://graph.facebook.com/" + apiVersion + "/" + phoneNumberId + "/messages";
            
            // Fazer a requisição para a API
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            
            logger.info("Teste hello_world enviado com sucesso para {}. Resposta: {}", 
                    telefoneNormalizado, response);
            
            return true;
        } catch (Exception e) {
            logger.error("Erro ao enviar teste hello_world via WhatsApp para {}: {}", 
                    telefone, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Normaliza um número de telefone para o formato usado pela API do WhatsApp
     * Remove espaços, parênteses, traços e adiciona o código do país se necessário
     */
    private String normalizarTelefone(String telefone) {
        // Remover caracteres não numéricos
        String apenasNumeros = telefone.replaceAll("[^0-9]", "");
        
        // Se não começar com 55 (código do Brasil), adicionar
        if (!apenasNumeros.startsWith("55")) {
            apenasNumeros = "55" + apenasNumeros;
        }
        
        return apenasNumeros;
    }
}
