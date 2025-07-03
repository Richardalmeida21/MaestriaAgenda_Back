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

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/notificacoes")
public class NotificacaoController {
    
    private final AgendamentoReminderService reminderService;
    private final NotificacaoService notificacaoService;
    
    public NotificacaoController(
            AgendamentoReminderService reminderService,
            NotificacaoService notificacaoService) {
        this.reminderService = reminderService;
        this.notificacaoService = notificacaoService;
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
}
