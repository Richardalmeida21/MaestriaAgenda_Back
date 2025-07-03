package com.maestria.agenda.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.maestria.agenda.agendamento.Agendamento;
import com.maestria.agenda.agendamento.AgendamentoRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class AgendamentoReminderService {

    private static final Logger logger = LoggerFactory.getLogger(AgendamentoReminderService.class);
    
    private final AgendamentoRepository agendamentoRepository;
    private final NotificacaoService notificacaoService;
    
    public AgendamentoReminderService(
            AgendamentoRepository agendamentoRepository,
            NotificacaoService notificacaoService) {
        this.agendamentoRepository = agendamentoRepository;
        this.notificacaoService = notificacaoService;
    }
    
    /**
     * Executa todos os dias às 10:00 da manhã para enviar lembretes 
     * sobre os agendamentos do dia seguinte
     */
    @Scheduled(cron = "0 0 10 * * ?", zone = "America/Sao_Paulo")
    public void enviarLembretesAgendamentos() {
        logger.info("Iniciando envio de lembretes de agendamentos");
        
        try {
            // Obtém a data de amanhã
            LocalDate amanha = LocalDate.now(ZoneId.of("America/Sao_Paulo")).plusDays(1);
            
            // Busca todos os agendamentos para amanhã
            List<Agendamento> agendamentosAmanha = agendamentoRepository.findByData(amanha);
            
            logger.info("Encontrados {} agendamentos para amanhã ({})", agendamentosAmanha.size(), amanha);
            
            int sucessos = 0;
            int falhas = 0;
            
            // Envia mensagem para cada agendamento
            for (Agendamento agendamento : agendamentosAmanha) {
                try {
                    boolean enviado = notificacaoService.enviarLembreteWhatsApp(agendamento);
                    if (enviado) {
                        sucessos++;
                    } else {
                        falhas++;
                    }
                } catch (Exception e) {
                    logger.error("Erro ao processar lembrete para agendamento ID {}: {}", 
                            agendamento.getId(), e.getMessage(), e);
                    falhas++;
                }
            }
            
            logger.info("Processamento de lembretes concluído. Sucessos: {}, Falhas: {}", sucessos, falhas);
        } catch (Exception e) {
            logger.error("Erro no processamento de lembretes: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Envia um lembrete de teste para um agendamento específico
     * Útil para testar a configuração do serviço de notificação
     */
    public boolean enviarLembreteManual(Long agendamentoId) {
        try {
            Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                    .orElseThrow(() -> new RuntimeException("Agendamento não encontrado com ID: " + agendamentoId));
            
            return notificacaoService.enviarLembreteWhatsApp(agendamento);
        } catch (Exception e) {
            logger.error("Erro ao enviar lembrete manual para agendamento ID {}: {}", 
                    agendamentoId, e.getMessage(), e);
            return false;
        }
    }
}
