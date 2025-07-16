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
    private final WppConnectService wppConnectService;
    
    @Value("${whatsapp.cloud.api.token:#{null}}")
    private String apiToken;
    
    @Value("${whatsapp.cloud.api.phone.number.id:#{null}}")
    private String phoneNumberId;
    
    @Value("${whatsapp.enabled:false}")
    private boolean whatsappEnabled;
    
    @Value("${whatsapp.cloud.api.version:v17.0}")
    private String apiVersion;

    @Value("${notification.provider}")
    private String notificationProvider;
    
    public NotificacaoService(WebClient whatsappApiClient, ObjectMapper objectMapper, WppConnectService wppConnectService) {
        this.whatsappApiClient = whatsappApiClient;
        this.objectMapper = objectMapper;
        this.wppConnectService = wppConnectService;
    }
    
    /**
     * Envia uma mensagem de lembrete de agendamento pelo WhatsApp
     * 
     * @param agendamento O agendamento para o qual enviar o lembrete
     * @return true se a mensagem foi enviada com sucesso, false caso contrário
     */
    public boolean enviarLembreteWhatsApp(Agendamento agendamento) {
        if ("wppconnect".equalsIgnoreCase(notificationProvider)) {
            String mensagem = criarMensagemLembrete(agendamento);
            return wppConnectService.enviarMensagem(agendamento.getCliente().getTelefone(), mensagem);
        }

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
        if ("wppconnect".equalsIgnoreCase(notificationProvider)) {
            return wppConnectService.enviarMensagem(telefone, mensagem);
        }

        if (!whatsappEnabled) {
            logger.info("WhatsApp API está desativada. Simulando envio de mensagem para: {}", telefone);
            return true;
        }
        
        // Verificar se as configurações necessárias estão presentes
        if (apiToken == null || apiToken.trim().isEmpty() || phoneNumberId == null || phoneNumberId.trim().isEmpty()) {
            logger.error("Configuração de WhatsApp incompleta. Token API ou Phone Number ID não configurados.");
            return false;
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
            
            // Usamos um template para iniciar a conversa em vez de mensagem de texto direta
            requestBody.put("type", "template");
            
            ObjectNode templateNode = objectMapper.createObjectNode();
            templateNode.put("name", "hello_world");
            
            ObjectNode languageNode = objectMapper.createObjectNode();
            languageNode.put("code", "en_US");
            templateNode.set("language", languageNode);
            
            // Se precisarmos passar parâmetros para o template, adicionaríamos aqui
            // ObjectNode componentsNode = objectMapper.createObjectNode();
            // templateNode.set("components", componentsNode);
            
            requestBody.set("template", templateNode);
            
            // Log do payload para depuração
            logger.info("Enviando mensagem WhatsApp: {}", requestBody.toString());
            
            // Log dos detalhes completos da requisição
            logger.info("Detalhes da requisição WhatsApp:");
            logger.info(" - URL: {}", "https://graph.facebook.com/" + apiVersion + "/" + phoneNumberId + "/messages");
            logger.info(" - Phone Number ID: {}", phoneNumberId);
            logger.info(" - Token configurado: {}", apiToken != null ? "Presente (tamanho: " + apiToken.length() + ")" : "Ausente");
            logger.info(" - Telefone destino (original): {}", telefone);
            logger.info(" - Telefone destino (formatado): {}", telefoneFormatado);
            logger.info(" - Payload: {}", requestBody.toString());
            
            try {
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
                logger.error("Erro específico ao enviar mensagem para a API do WhatsApp: {}", e.getMessage());
                logger.error("Detalhes do erro:", e);
                return false;
            }
        } catch (Exception e) {
            logger.error("Erro ao enviar mensagem WhatsApp: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Envia uma mensagem de texto (só funciona dentro da janela de 24h após receber uma mensagem do usuário)
     * 
     * @param telefone O número de telefone de destino
     * @param mensagem A mensagem a ser enviada
     * @return true se a mensagem foi enviada com sucesso, false caso contrário
     */
    public boolean enviarMensagemTexto(String telefone, String mensagem) {
        if (!whatsappEnabled) {
            logger.info("WhatsApp API está desativada. Simulando envio de mensagem de texto para: {}", telefone);
            return true;
        }
        
        // Verificar se as configurações necessárias estão presentes
        if (apiToken == null || apiToken.trim().isEmpty() || phoneNumberId == null || phoneNumberId.trim().isEmpty()) {
            logger.error("Configuração de WhatsApp incompleta. Token API ou Phone Number ID não configurados.");
            return false;
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
            logger.info("Enviando mensagem de texto WhatsApp: {}", requestBody.toString());
            
            // Log dos detalhes completos da requisição
            logger.info("Detalhes da requisição WhatsApp (texto):");
            logger.info(" - URL: {}", "https://graph.facebook.com/" + apiVersion + "/" + phoneNumberId + "/messages");
            logger.info(" - Phone Number ID: {}", phoneNumberId);
            logger.info(" - Telefone destino (formatado): {}", telefoneFormatado);
            
            try {
                // Enviar a mensagem usando o WebClient
                String response = whatsappApiClient.post()
                        .uri("/" + phoneNumberId + "/messages")
                        .header("Authorization", "Bearer " + apiToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(requestBody), ObjectNode.class)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                
                logger.info("Mensagem de texto enviada com sucesso. Resposta: {}", response);
                return true;
            } catch (Exception e) {
                logger.error("Erro específico ao enviar mensagem de texto para a API do WhatsApp: {}", e.getMessage());
                logger.error("Detalhes do erro:", e);
                return false;
            }
        } catch (Exception e) {
            logger.error("Erro ao enviar mensagem de texto WhatsApp: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Envia uma mensagem usando o template lembrete_agendamento
     * 
     * @param telefone O número de telefone do cliente
     * @param nomeCliente O nome do cliente
     * @param dataAgendamento A data do agendamento (formato DD/MM/YYYY)
     * @param servico O nome do serviço
     * @param nomeProfissional O nome do profissional
     * @return true se a mensagem foi enviada com sucesso, false caso contrário
     */
    public boolean enviarLembreteAgendamentoTemplate(String telefone, String nomeCliente, 
                                                   String dataAgendamento, String servico, 
                                                   String nomeProfissional) {
        if ("wppconnect".equalsIgnoreCase(notificationProvider)) {
            String mensagem = String.format(
                "Olá %s! Lembrete de agendamento para %s: %s com %s.",
                nomeCliente, dataAgendamento, servico, nomeProfissional
            );
            return wppConnectService.enviarMensagem(telefone, mensagem);
        }

        if (!whatsappEnabled) {
            logger.info("WhatsApp API está desativada. Simulando envio de lembrete de agendamento para: {}", telefone);
            return true;
        }
        
        // Verificar se as configurações necessárias estão presentes
        if (apiToken == null || apiToken.trim().isEmpty() || phoneNumberId == null || phoneNumberId.trim().isEmpty()) {
            logger.error("Configuração de WhatsApp incompleta. Token API ou Phone Number ID não configurados.");
            return false;
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
            requestBody.put("type", "template");
            
            ObjectNode templateNode = objectMapper.createObjectNode();
            templateNode.put("name", "lembrete_agendamento_v2");
            
            ObjectNode languageNode = objectMapper.createObjectNode();
            languageNode.put("code", "pt_BR");
            templateNode.set("language", languageNode);
            
            // Adicionar componentes do template com os parâmetros
            com.fasterxml.jackson.databind.node.ArrayNode componentsArray = objectMapper.createArrayNode();
            
            // Componente de texto do corpo da mensagem
            ObjectNode textComponent = objectMapper.createObjectNode();
            textComponent.put("type", "body");
            
            // Parâmetros do corpo do texto
            com.fasterxml.jackson.databind.node.ArrayNode parametersArray = objectMapper.createArrayNode();
            
            // 1. Nome do cliente
            ObjectNode param1 = objectMapper.createObjectNode();
            param1.put("type", "text");
            param1.put("text", nomeCliente);
            parametersArray.add(param1);
            
            // 2. Data do agendamento
            ObjectNode param2 = objectMapper.createObjectNode();
            param2.put("type", "text");
            param2.put("text", dataAgendamento);
            parametersArray.add(param2);
            
            // 3. Nome do serviço
            ObjectNode param3 = objectMapper.createObjectNode();
            param3.put("type", "text");
            param3.put("text", servico);
            parametersArray.add(param3);
            
            // 4. Nome do profissional
            ObjectNode param4 = objectMapper.createObjectNode();
            param4.put("type", "text");
            param4.put("text", nomeProfissional);
            parametersArray.add(param4);
            
            textComponent.set("parameters", parametersArray);
            componentsArray.add(textComponent);
            
            templateNode.set("components", componentsArray);
            requestBody.set("template", templateNode);
            
            // Log do payload para depuração
            logger.info("Enviando lembrete de agendamento via WhatsApp: {}", requestBody.toString());
            
            // Log dos detalhes completos da requisição
            logger.info("Detalhes da requisição WhatsApp (lembrete de agendamento):");
            logger.info(" - URL: {}", "https://graph.facebook.com/" + apiVersion + "/" + phoneNumberId + "/messages");
            logger.info(" - Phone Number ID: {}", phoneNumberId);
            logger.info(" - Telefone destino (formatado): {}", telefoneFormatado);
            logger.info(" - Nome do cliente: {}", nomeCliente);
            logger.info(" - Data do agendamento: {}", dataAgendamento);
            logger.info(" - Serviço: {}", servico);
            logger.info(" - Profissional: {}", nomeProfissional);
            
            try {
                // Enviar a mensagem usando o WebClient
                String response = whatsappApiClient.post()
                        .uri("/" + phoneNumberId + "/messages")
                        .header("Authorization", "Bearer " + apiToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(requestBody), ObjectNode.class)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                
                logger.info("Lembrete de agendamento enviado com sucesso. Resposta: {}", response);
                return true;
            } catch (Exception e) {
                logger.error("Erro ao enviar lembrete de agendamento para a API do WhatsApp: {}", e.getMessage());
                logger.error("Detalhes do erro:", e);
                return false;
            }
        } catch (Exception e) {
            logger.error("Erro ao preparar lembrete de agendamento WhatsApp: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Envia um lembrete de agendamento usando o template personalizado para um agendamento existente
     * 
     * @param agendamento O agendamento para o qual enviar o lembrete
     * @return true se a mensagem foi enviada com sucesso, false caso contrário
     */
    public boolean enviarLembreteAgendamentoTemplate(Agendamento agendamento) {
        if (agendamento == null || agendamento.getCliente() == null) {
            logger.error("Agendamento ou cliente nulo. Não é possível enviar lembrete.");
            return false;
        }
        
        try {
            // Formatar a data no padrão brasileiro (DD/MM/YYYY)
            String dataFormatada = agendamento.getData().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            
            return enviarLembreteAgendamentoTemplate(
                agendamento.getCliente().getTelefone(),
                agendamento.getCliente().getNome(),
                dataFormatada,
                agendamento.getServico().getNome(),
                agendamento.getProfissional().getNome()
            );
        } catch (Exception e) {
            logger.error("Erro ao preparar lembrete de agendamento com template: {}", e.getMessage(), e);
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
        boolean tokenConfigurado = apiToken != null && !apiToken.isEmpty();
        status.put("tokenConfigurado", tokenConfigurado);
        
        // Verificar se o ID do número de telefone está configurado
        boolean phoneIdConfigurado = phoneNumberId != null && !phoneNumberId.isEmpty();
        status.put("phoneIdConfigurado", phoneIdConfigurado);
        
        // Verificar a versão da API
        status.put("apiVersion", apiVersion);
        
        // Status geral da configuração
        status.put("configuracaoCompleta", whatsappEnabled && tokenConfigurado && phoneIdConfigurado);
        
        // Adicionar URL dos endpoints de teste
        Map<String, Object> endpointsInfo = new HashMap<>();
        endpointsInfo.put("testeWhatsApp", Map.of(
            "endpoint", "/notificacoes/teste-whatsapp",
            "formato", "{\"telefone\": \"5511999999999\", \"mensagem\": \"Teste\"}"
        ));
        
        endpointsInfo.put("testeWhatsAppTexto", Map.of(
            "endpoint", "/notificacoes/teste-whatsapp-texto",
            "formato", "{\"telefone\": \"5511999999999\", \"mensagem\": \"Teste\"}"
        ));
        
        endpointsInfo.put("testeLembreteAgendamento", Map.of(
            "endpoint", "/notificacoes/teste-lembrete-agendamento",
            "formato", "{\"telefone\": \"5511999999999\", \"nomeCliente\": \"João Silva\", \"dataAgendamento\": \"01/08/2025\", \"servico\": \"Corte de Cabelo\", \"nomeProfissional\": \"Maria Oliveira\"}"
        ));
        
        status.put("endpointsTeste", endpointsInfo);
        
        // Templates disponíveis
        Map<String, Object> templatesInfo = new HashMap<>();
        templatesInfo.put("hello_world", Map.of(
            "idioma", "en_US",
            "descricao", "Template padrão para iniciar conversas"
        ));
        
        templatesInfo.put("lembrete_agendamento_v2", Map.of(
            "idioma", "pt_BR",
            "descricao", "Template personalizado para lembretes de agendamento",
            "variaveis", new String[]{"nome", "data_agendamento", "servico", "nome_profissional"}
        ));
        
        status.put("templatesDisponiveis", templatesInfo);
        
        // Próximos passos
        if (!whatsappEnabled) {
            status.put("proximoPasso", "Ativar WhatsApp configurando WHATSAPP_ENABLED=true");
        } else if (!tokenConfigurado) {
            status.put("proximoPasso", "Configurar token de API (WHATSAPP_API_TOKEN)");
        } else if (!phoneIdConfigurado) {
            status.put("proximoPasso", "Configurar ID do número de telefone (WHATSAPP_PHONE_NUMBER_ID)");
        } else {
            status.put("proximoPasso", "Configuração completa! Teste o envio de mensagens com um dos endpoints de teste");
        }
        
        return status;
    }
    
    /**
     * Formata o número de telefone para o formato aceito pelo WhatsApp
     * Removes caracteres não numéricos e adiciona o código do país se necessário
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
    
    /**
     * Envia uma mensagem de lembrete usando o template "lembrete_agendamento"
     * 
     * @param telefone Número de telefone do cliente
     * @param nome Nome do cliente
     * @param data Data formatada do agendamento
     * @param servico Serviço agendado
     * @param profissional Nome do profissional
     * @return true se a mensagem foi enviada com sucesso, false caso contrário
     */
    public boolean enviarLembreteTemplate(String telefone, String nome, String data, 
                                          String servico, String profissional) {
        if ("wppconnect".equalsIgnoreCase(notificationProvider)) {
            String mensagem = String.format(
                "Olá %s! Lembrete de agendamento para %s: %s com %s.",
                nome, data, servico, profissional
            );
            return wppConnectService.enviarMensagem(telefone, mensagem);
        }
        if (!whatsappEnabled || apiToken == null || phoneNumberId == null) {
            logger.warn("Notificações WhatsApp desabilitadas ou configuração incompleta. Token: {}, Phone ID: {}", 
                    apiToken != null ? "configurado" : "não configurado", 
                    phoneNumberId != null ? "configurado" : "não configurado");
            return false;
        }
        
        try {
            // Normalizar o número de telefone
            String numeroNormalizado = formatarNumeroTelefone(telefone);
            
            logger.info("Enviando lembrete por template para {} ({}): {}, {}, {}", 
                    nome, numeroNormalizado, data, servico, profissional);
            
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("messaging_product", "whatsapp");
            requestBody.put("to", numeroNormalizado);
            requestBody.put("type", "template");
            
            ObjectNode templateNode = objectMapper.createObjectNode();
            templateNode.put("name", "lembrete_agendamento_v2");
            
            ObjectNode languageNode = objectMapper.createObjectNode();
            languageNode.put("code", "pt_BR");
            templateNode.set("language", languageNode);
            
            // Configurando as variáveis do template
            
            // Criar o componente de corpo
            ObjectNode bodyComponent = objectMapper.createObjectNode();
            bodyComponent.put("type", "body");
            
            // Criar array de parâmetros
            com.fasterxml.jackson.databind.node.ArrayNode parametersArray = objectMapper.createArrayNode();
            
            // Parâmetro 1: Nome
            ObjectNode param1 = objectMapper.createObjectNode();
            param1.put("type", "text");
            param1.put("text", nome);
            parametersArray.add(param1);
            
            // Parâmetro 2: Data
            ObjectNode param2 = objectMapper.createObjectNode();
            param2.put("type", "text");
            param2.put("text", data);
            parametersArray.add(param2);
            
            // Parâmetro 3: Serviço
            ObjectNode param3 = objectMapper.createObjectNode();
            param3.put("type", "text");
            param3.put("text", servico);
            parametersArray.add(param3);
            
            // Parâmetro 4: Profissional
            ObjectNode param4 = objectMapper.createObjectNode();
            param4.put("type", "text");
            param4.put("text", profissional);
            parametersArray.add(param4);
            
            // Adicionar array de parâmetros ao componente de corpo
            bodyComponent.set("parameters", parametersArray);
            
            // Criar array de componentes e adicionar o componente de corpo
            com.fasterxml.jackson.databind.node.ArrayNode componentsArray = objectMapper.createArrayNode();
            componentsArray.add(bodyComponent);
            
            // Adicionar array de componentes ao template
            templateNode.set("components", componentsArray);
            requestBody.set("template", templateNode);
            
            // Enviar a requisição
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);
            logger.debug("Corpo da requisição WhatsApp: {}", requestBodyJson);
            
            String apiUrl = "/" + phoneNumberId + "/messages";
            
            return whatsappApiClient.post()
                .uri(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiToken)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    logger.info("Resposta do WhatsApp API: {}", response);
                    return true;
                })
                .onErrorResume(error -> {
                    logger.error("Erro ao enviar lembrete por template: {}", error.getMessage(), error);
                    return Mono.just(false);
                })
                .block();
        } catch (Exception e) {
            logger.error("Erro ao construir a requisição de lembrete por template: {}", e.getMessage(), e);
            return false;
        }
    }

    private String normalizarTelefone(String telefone) {
        if (telefone == null) return null;
        return telefone.replaceAll("[^0-9]", "");
    }
}
