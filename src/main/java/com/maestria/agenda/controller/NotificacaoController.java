package com.maestria.agenda.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.maestria.agenda.service.AgendamentoReminderService;
import com.maestria.agenda.service.NotificacaoService;
import com.maestria.agenda.service.WhatsAppService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/notificacoes")
public class NotificacaoController {
    
    private final AgendamentoReminderService reminderService;
    private final NotificacaoService notificacaoService;
    private final WhatsAppService whatsAppService;
    
    public NotificacaoController(
            AgendamentoReminderService reminderService,
            NotificacaoService notificacaoService,
            WhatsAppService whatsAppService) {
        this.reminderService = reminderService;
        this.notificacaoService = notificacaoService;
        this.whatsAppService = whatsAppService;
    }
    
    /**
     * Endpoint para enviar um lembrete de agendamento manualmente
     * Restrito a usuários com papel ADMIN
     */
    @PostMapping("/enviar-lembrete/{agendamentoId}")
    public ResponseEntity<?> enviarLembreteManual(
            @PathVariable Long agendamentoId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // Verifica se o usuário tem permissão de administrador
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode enviar lembretes manualmente.");
        }
        
        boolean enviado = reminderService.enviarLembreteManual(agendamentoId);
        
        if (enviado) {
            return ResponseEntity.ok("Lembrete enviado com sucesso");
        } else {
            return ResponseEntity.status(500).body("Falha ao enviar lembrete");
        }
    }
    
    /**
     * Endpoint para verificar o status da configuração do WhatsApp
     * Útil para testar se as variáveis de ambiente estão configuradas corretamente
     */
    @GetMapping("/status-configuracao")
    public ResponseEntity<?> verificarStatusConfiguracao(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // Verifica se o usuário tem permissão de administrador
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode verificar o status da configuração.");
        }
        
        Map<String, Object> status = notificacaoService.verificarConfiguracao();
        return ResponseEntity.ok(status);
    }
    
    /**
     * Endpoint para testar o envio de mensagem WhatsApp diretamente para um número
     * Útil para testar a configuração sem precisar de um agendamento existente
     */
    @PostMapping("/teste-whatsapp")
    public ResponseEntity<?> testarEnvioWhatsApp(
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // Verifica se o usuário tem permissão de administrador
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode testar o envio de WhatsApp.");
        }
        
        String telefone = payload.get("telefone");
        String mensagem = payload.get("mensagem");
        
        if (telefone == null || telefone.isEmpty()) {
            return ResponseEntity.badRequest().body("Telefone é obrigatório");
        }
        
        if (mensagem == null || mensagem.isEmpty()) {
            mensagem = "Esta é uma mensagem de teste do sistema de notificação MaestriaAgenda.";
        }
        
        boolean enviado = notificacaoService.enviarMensagemDireta(telefone, mensagem);
        
        if (enviado) {
            return ResponseEntity.ok("Mensagem de teste enviada com sucesso");
        } else {
            return ResponseEntity.status(500).body("Falha ao enviar mensagem de teste");
        }
    }
    
    /**
     * Endpoint para testar o envio de mensagem de texto pelo WhatsApp
     * Só funciona após iniciar uma conversa com o template
     */
    @PostMapping("/teste-whatsapp-texto")
    public ResponseEntity<?> testarEnvioWhatsAppTexto(
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // Verifica se o usuário tem permissão de administrador
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode testar o envio de WhatsApp.");
        }
        
        String telefone = payload.get("telefone");
        String mensagem = payload.get("mensagem");
        
        if (telefone == null || telefone.isEmpty()) {
            return ResponseEntity.badRequest().body("Telefone é obrigatório");
        }
        
        if (mensagem == null || mensagem.isEmpty()) {
            mensagem = "Esta é uma mensagem de texto direta do sistema MaestriaAgenda.";
        }
        
        boolean enviado = notificacaoService.enviarMensagemTexto(telefone, mensagem);
        
        if (enviado) {
            return ResponseEntity.ok("Mensagem de texto enviada com sucesso");
        } else {
            return ResponseEntity.status(500).body("Falha ao enviar mensagem de texto");
        }
    }
    
    /**
     * Endpoint para testar o envio de lembrete de agendamento usando o template personalizado
     * Usa o template "lembrete_agendamento" com as variáveis definidas
     */
    @PostMapping("/teste-lembrete-agendamento")
    public ResponseEntity<?> testarLembreteAgendamento(
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // Verifica se o usuário tem permissão de administrador
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode testar o envio de lembretes.");
        }
        
        String telefone = payload.get("telefone");
        String nomeCliente = payload.get("nomeCliente");
        String dataAgendamento = payload.get("dataAgendamento");
        String servico = payload.get("servico");
        String nomeProfissional = payload.get("nomeProfissional");
        
        // Validações básicas
        Map<String, String> camposFaltantes = new HashMap<>();
        
        if (telefone == null || telefone.isEmpty()) camposFaltantes.put("telefone", "Telefone é obrigatório");
        if (nomeCliente == null || nomeCliente.isEmpty()) camposFaltantes.put("nomeCliente", "Nome do cliente é obrigatório");
        if (dataAgendamento == null || dataAgendamento.isEmpty()) camposFaltantes.put("dataAgendamento", "Data do agendamento é obrigatória");
        if (servico == null || servico.isEmpty()) camposFaltantes.put("servico", "Nome do serviço é obrigatório");
        if (nomeProfissional == null || nomeProfissional.isEmpty()) camposFaltantes.put("nomeProfissional", "Nome do profissional é obrigatório");
        
        if (!camposFaltantes.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "mensagem", "Campos obrigatórios não informados",
                "camposFaltantes", camposFaltantes
            ));
        }
        
        // Envia o lembrete de agendamento
        boolean enviado = notificacaoService.enviarLembreteAgendamentoTemplate(
            telefone, nomeCliente, dataAgendamento, servico, nomeProfissional
        );
        
        if (enviado) {
            return ResponseEntity.ok(Map.of(
                "sucesso", true,
                "mensagem", "Lembrete de agendamento enviado com sucesso",
                "detalhes", Map.of(
                    "telefone", telefone,
                    "nomeCliente", nomeCliente,
                    "dataAgendamento", dataAgendamento,
                    "servico", servico,
                    "nomeProfissional", nomeProfissional
                )
            ));
        } else {
            return ResponseEntity.status(500).body(Map.of(
                "sucesso", false,
                "mensagem", "Falha ao enviar lembrete de agendamento"
            ));
        }
    }
    
    /**
     * Endpoint para testar o envio de lembrete de agendamento usando o novo template
     * Restrito a usuários com papel ADMIN
     */
    @PostMapping("/teste-template")
    public ResponseEntity<?> testarTemplateAgendamento(
            @RequestBody Map<String, String> dados,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // Verifica se o usuário tem permissão de administrador
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode testar o template.");
        }
        
        // Extrair dados do payload
        String telefone = dados.get("telefone");
        String nome = dados.get("nome");
        String data = dados.get("data");
        String servico = dados.get("servico");
        String profissional = dados.get("profissional");
        
        // Validar dados obrigatórios
        if (telefone == null || nome == null || data == null || servico == null || profissional == null) {
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("sucesso", false);
            resposta.put("erro", "Todos os campos são obrigatórios: telefone, nome, data, servico, profissional");
            return ResponseEntity.badRequest().body(resposta);
        }
        
        boolean enviado = notificacaoService.enviarLembreteTemplate(
                telefone, nome, data, servico, profissional);
        
        Map<String, Object> resposta = new HashMap<>();
        resposta.put("sucesso", enviado);
        resposta.put("mensagem", enviado ? 
                "Lembrete enviado com sucesso" : 
                "Falha ao enviar lembrete. Verifique os logs para mais detalhes.");
        
        return ResponseEntity.ok(resposta);
    }
    
    /**
     * Endpoint para testar o template de lembrete_agendamento
     * Restrito a usuários com papel ADMIN
     */
    @PostMapping("/teste-template-lembrete")
    public ResponseEntity<?> testarTemplateLembrete(
            @RequestBody Map<String, String> dados,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // Verifica se o usuário tem permissão de administrador
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode testar templates.");
        }
        
        try {
            String telefone = dados.get("telefone");
            String nome = dados.get("nome");
            String data = dados.get("data");
            String servico = dados.get("servico");
            String profissional = dados.get("profissional");
            
            // Validar dados obrigatórios
            if (telefone == null || nome == null || data == null || 
                servico == null || profissional == null) {
                Map<String, Object> resposta = new HashMap<>();
                resposta.put("sucesso", false);
                resposta.put("mensagem", "Todos os campos são obrigatórios: telefone, nome, data, servico, profissional");
                return ResponseEntity.badRequest().body(resposta);
            }
            
            boolean enviado = notificacaoService.enviarLembreteAgendamentoTemplate(
                    telefone, nome, data, servico, profissional);
            
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("sucesso", enviado);
            resposta.put("mensagem", enviado ? 
                    "Lembrete de teste enviado com sucesso!" : 
                    "Falha ao enviar lembrete. Verifique os logs para mais detalhes.");
            
            return ResponseEntity.ok(resposta);
        } catch (Exception e) {
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("sucesso", false);
            resposta.put("mensagem", "Erro ao processar requisição: " + e.getMessage());
            return ResponseEntity.status(500).body(resposta);
        }
    }
    
    /**
     * Endpoint para testar o template hello_world (básico)
     * Restrito a usuários com papel ADMIN
     */
    @PostMapping("/teste-hello-world")
    public ResponseEntity<?> testarHelloWorld(
            @RequestBody Map<String, String> dados,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // Verifica se o usuário tem permissão de administrador
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode testar templates.");
        }
        
        try {
            String telefone = dados.get("telefone");
            
            // Validar telefone
            if (telefone == null || telefone.isEmpty()) {
                Map<String, Object> resposta = new HashMap<>();
                resposta.put("sucesso", false);
                resposta.put("mensagem", "O campo telefone é obrigatório");
                return ResponseEntity.badRequest().body(resposta);
            }
            
            boolean enviado = notificacaoService.enviarMensagemDireta(telefone, "Olá, este é um teste do template Hello World!");
            
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("sucesso", enviado);
            resposta.put("mensagem", enviado ? 
                    "Mensagem Hello World enviada com sucesso!" : 
                    "Falha ao enviar mensagem. Verifique os logs para mais detalhes.");
            
            return ResponseEntity.ok(resposta);
        } catch (Exception e) {
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("sucesso", false);
            resposta.put("mensagem", "Erro ao processar requisição: " + e.getMessage());
            return ResponseEntity.status(500).body(resposta);
        }
    }
}
