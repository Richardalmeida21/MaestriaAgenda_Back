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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
            request.put("messaging_product", "whatsapp");
            request.put("recipient_type", "individual");
            request.put("to", telefoneNormalizado);
            request.put("type", "template");
            
            Map<String, Object> template = new HashMap<>();
            template.put("name", TEMPLATE_LEMBRETE_AGENDAMENTO);
            
            Map<String, String> language = new HashMap<>();
            language.put("code", "pt_BR");
            template.put("language", language);
            
            // Criar a lista de componentes
            List<Map<String, Object>> components = new ArrayList<>();
            
            // Adicionar o componente body
            Map<String, Object> bodyComponent = new HashMap<>();
            bodyComponent.put("type", "body");
            
            // Configurar os parâmetros como uma lista
            List<Map<String, Object>> parameters = new ArrayList<>();
            
            // Parâmetro 1: Nome
            Map<String, Object> param1 = new HashMap<>();
            param1.put("type", "text");
            param1.put("text", nome);
            parameters.add(param1);
            
            // Parâmetro 2: Data
            Map<String, Object> param2 = new HashMap<>();
            param2.put("type", "text");
            param2.put("text", dataFormatada);
            parameters.add(param2);
            
            // Parâmetro 3: Serviço
            Map<String, Object> param3 = new HashMap<>();
            param3.put("type", "text");
            param3.put("text", servico);
            parameters.add(param3);
            
            // Parâmetro 4: Profissional
            Map<String, Object> param4 = new HashMap<>();
            param4.put("type", "text");
            param4.put("text", profissional);
            parameters.add(param4);
            
            bodyComponent.put("parameters", parameters);
            components.add(bodyComponent);
            
            template.put("components", components);
            request.put("template", template);
            
            // Configurar cabeçalhos
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiToken);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            // URL da API do WhatsApp
            String url = "https://graph.facebook.com/" + apiVersion + "/" + phoneNumberId + "/messages";
            
            // Fazer a requisição para a API
            @SuppressWarnings("unchecked")
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
            
            // Montar o payload para a API do WhatsApp seguindo exatamente o formato requerido
            Map<String, Object> request = new HashMap<>();
            request.put("messaging_product", "whatsapp");
            request.put("recipient_type", "individual"); // Este campo é importante e estava faltando
            request.put("to", telefoneNormalizado);
            request.put("type", "template");
            
            Map<String, Object> template = new HashMap<>();
            template.put("name", TEMPLATE_LEMBRETE_AGENDAMENTO);
            
            Map<String, String> language = new HashMap<>();
            language.put("code", "pt_BR");
            template.put("language", language);
            
            // Criar a lista de componentes
            List<Map<String, Object>> components = new ArrayList<>();
            
            // Adicionar o componente body
            Map<String, Object> bodyComponent = new HashMap<>();
            bodyComponent.put("type", "body");
            
            // Configurar os parâmetros como uma lista (não como array)
            List<Map<String, Object>> parameters = new ArrayList<>();
            
            // Parâmetro 1: Nome
            Map<String, Object> param1 = new HashMap<>();
            param1.put("type", "text");
            param1.put("text", nome);
            parameters.add(param1);
            
            // Parâmetro 2: Data
            Map<String, Object> param2 = new HashMap<>();
            param2.put("type", "text");
            param2.put("text", dataString);
            parameters.add(param2);
            
            // Parâmetro 3: Serviço
            Map<String, Object> param3 = new HashMap<>();
            param3.put("type", "text");
            param3.put("text", servico);
            parameters.add(param3);
            
            // Parâmetro 4: Profissional
            Map<String, Object> param4 = new HashMap<>();
            param4.put("type", "text");
            param4.put("text", profissional);
            parameters.add(param4);
            
            bodyComponent.put("parameters", parameters);
            components.add(bodyComponent);
            
            template.put("components", components);
            request.put("template", template);
            
            // Configurar cabeçalhos
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiToken);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            // URL da API do WhatsApp
            String url = "https://graph.facebook.com/" + apiVersion + "/" + phoneNumberId + "/messages";
            
            // Log completo do payload para debug
            logger.info("Payload de teste completo: {}", request);
            
            // Fazer a requisição para a API
            @SuppressWarnings("unchecked")
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
            request.put("recipient_type", "individual"); // Adicionar recipient_type
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
            @SuppressWarnings("unchecked")
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
