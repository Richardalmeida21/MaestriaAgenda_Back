package com.maestria.agenda.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.maestria.agenda.agendamento.Agendamento;
import com.maestria.agenda.agendamento.AgendamentoRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class AgendamentoReminderServiceTest {

    @Autowired
    private AgendamentoReminderService reminderService;

    @MockBean
    private AgendamentoRepository agendamentoRepository;

    @MockBean
    private NotificacaoService notificacaoService;

    @Test
    void testEnviarLembreteManual() {
        // Preparação
        Long agendamentoId = 1L;
        Agendamento agendamento = new Agendamento();
        
        when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));
        when(notificacaoService.enviarLembreteWhatsApp(agendamento)).thenReturn(true);
        
        // Execução
        boolean resultado = reminderService.enviarLembreteManual(agendamentoId);
        
        // Verificação
        assertTrue(resultado);
        verify(agendamentoRepository).findById(agendamentoId);
        verify(notificacaoService).enviarLembreteWhatsApp(agendamento);
    }

    @Test
    void testEnviarLembretesAgendamentos() {
        // Preparação
        LocalDate amanha = LocalDate.now().plusDays(1);
        List<Agendamento> agendamentos = new ArrayList<>();
        Agendamento agendamento1 = new Agendamento();
        Agendamento agendamento2 = new Agendamento();
        agendamentos.add(agendamento1);
        agendamentos.add(agendamento2);
        
        when(agendamentoRepository.findByData(any(LocalDate.class))).thenReturn(agendamentos);
        when(notificacaoService.enviarLembreteWhatsApp(any(Agendamento.class))).thenReturn(true);
        
        // Execução
        reminderService.enviarLembretesAgendamentos();
        
        // Verificação
        verify(agendamentoRepository).findByData(any(LocalDate.class));
        verify(notificacaoService, times(2)).enviarLembreteWhatsApp(any(Agendamento.class));
    }
}
