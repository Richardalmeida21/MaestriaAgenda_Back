package com.maestria.agenda.service;

import com.maestria.agenda.agendamento.Agendamento;
import com.maestria.agenda.agendamento.AgendamentoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class LembreteDiarioService {

    private static final Logger logger = LoggerFactory.getLogger(LembreteDiarioService.class);

    private final AgendamentoRepository agendamentoRepository;
    private final NotificacaoService notificacaoService;

    public LembreteDiarioService(AgendamentoRepository agendamentoRepository, NotificacaoService notificacaoService) {
        this.agendamentoRepository = agendamentoRepository;
        this.notificacaoService = notificacaoService;
    }

    /**
     * Este método é executado todos os dias às 9h da manhã para enviar lembretes
     * de agendamentos para o dia seguinte.
     * A expressão cron "0 0 9 * * ?" significa:
     * - 0 segundos
     * - 0 minutos
     * - 9 horas
     * - * (todos os dias do mês)
     * - * (todos os meses)
     * - ? (qualquer dia da semana)
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void enviarLembretesDiarios() {
        LocalDate amanha = LocalDate.now().plusDays(1);
        logger.info("Iniciando tarefa de envio de lembretes para agendamentos do dia: {}", amanha);

        List<Agendamento> agendamentosDeAmanha = agendamentoRepository.findByData(amanha);

        if (agendamentosDeAmanha.isEmpty()) {
            logger.info("Nenhum agendamento encontrado para amanhã ({}). Nenhuma notificação será enviada.", amanha);
            return;
        }

        logger.info("Encontrados {} agendamentos para amanhã. Enviando notificações...", agendamentosDeAmanha.size());

        for (Agendamento agendamento : agendamentosDeAmanha) {
            try {
                logger.info("Enviando lembrete para o agendamento ID: {}", agendamento.getId());
                boolean enviado = notificacaoService.enviarLembreteAgendamentoTemplate(agendamento);
                if (enviado) {
                    logger.info("Lembrete para o agendamento ID {} enviado com sucesso.", agendamento.getId());
                } else {
                    logger.error("Falha ao enviar lembrete para o agendamento ID {}.", agendamento.getId());
                }
            } catch (Exception e) {
                logger.error("Erro ao processar lembrete para o agendamento ID {}: {}", agendamento.getId(), e.getMessage(), e);
            }
        }

        logger.info("Tarefa de envio de lembretes concluída.");
    }
}
