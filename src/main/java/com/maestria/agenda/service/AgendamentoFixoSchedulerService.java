package com.maestria.agenda.service;

import com.maestria.agenda.agendamento.Agendamento;
import com.maestria.agenda.agendamento.AgendamentoFixo;
import com.maestria.agenda.agendamento.AgendamentoFixoRepository;
import com.maestria.agenda.agendamento.AgendamentoRepository;
import com.maestria.agenda.bloqueio.BloqueioAgenda;
import com.maestria.agenda.bloqueio.BloqueioAgendaRepository;
import com.maestria.agenda.profissional.Profissional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Serviço responsável por gerar automaticamente as ocorrências futuras
 * dos agendamentos fixos (recorrentes).
 * 
 * Executa semanalmente aos domingos à meia-noite para garantir que sempre
 * haja agendamentos gerados para os próximos 60 dias.
 */
@Service
public class AgendamentoFixoSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(AgendamentoFixoSchedulerService.class);
    
    // Gera ocorrências para os próximos 60 dias (2 meses)
    private static final int DIAS_FUTUROS = 60;

    private final AgendamentoFixoRepository agendamentoFixoRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final BloqueioAgendaRepository bloqueioRepository;

    public AgendamentoFixoSchedulerService(
            AgendamentoFixoRepository agendamentoFixoRepository,
            AgendamentoRepository agendamentoRepository,
            BloqueioAgendaRepository bloqueioRepository) {
        this.agendamentoFixoRepository = agendamentoFixoRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.bloqueioRepository = bloqueioRepository;
    }

    /**
     * Job executado todos os domingos à meia-noite (00:00)
     * Expressão cron: segundo minuto hora dia-do-mês mês dia-da-semana
     * 0 0 0 * * 0 = 00:00 de todo domingo
     */
    @Scheduled(cron = "0 0 0 * * 0", zone = "America/Sao_Paulo")
    @Transactional
    public void gerarOcorrenciasFuturas() {
        logger.info("🔄 [SCHEDULER] Iniciando geração de ocorrências futuras de agendamentos fixos...");
        
        try {
            // Busca todos os agendamentos fixos ativos
            List<AgendamentoFixo> agendamentosFixos = agendamentoFixoRepository.findByAtivoTrue();
            
            if (agendamentosFixos.isEmpty()) {
                logger.info("ℹ️ [SCHEDULER] Nenhum agendamento fixo ativo encontrado.");
                return;
            }
            
            logger.info("📋 [SCHEDULER] Encontrados {} agendamentos fixos ativos", agendamentosFixos.size());
            
            LocalDate hoje = LocalDate.now();
            LocalDate dataFimGeracao = hoje.plusDays(DIAS_FUTUROS);
            
            int totalOcorrenciasCriadas = 0;
            int totalOcorrenciasJaExistentes = 0;
            
            for (AgendamentoFixo agendamentoFixo : agendamentosFixos) {
                try {
                    int ocorrenciasCriadas = gerarOcorrenciasParaAgendamentoFixo(
                        agendamentoFixo, hoje, dataFimGeracao
                    );
                    totalOcorrenciasCriadas += ocorrenciasCriadas;
                } catch (Exception e) {
                    logger.error("❌ [SCHEDULER] Erro ao gerar ocorrências para agendamento fixo ID {}: {}",
                        agendamentoFixo.getId(), e.getMessage(), e);
                }
            }
            
            logger.info("✅ [SCHEDULER] Geração concluída! {} novas ocorrências criadas, {} já existentes",
                totalOcorrenciasCriadas, totalOcorrenciasJaExistentes);
                
        } catch (Exception e) {
            logger.error("❌ [SCHEDULER] Erro crítico ao executar job de geração de agendamentos fixos", e);
        }
    }

    /**
     * Gera ocorrências para um agendamento fixo específico no período informado
     */
    private int gerarOcorrenciasParaAgendamentoFixo(
            AgendamentoFixo agendamentoFixo, 
            LocalDate dataInicio, 
            LocalDate dataFim) {
        
        int ocorrenciasCriadas = 0;
        LocalDate dataAtual = dataInicio;
        
        // Se a data de início do agendamento fixo é futura, começar por ela
        if (agendamentoFixo.getDataInicio() != null && agendamentoFixo.getDataInicio().isAfter(dataAtual)) {
            dataAtual = agendamentoFixo.getDataInicio();
        }
        
        while (!dataAtual.isAfter(dataFim)) {
            // Verifica se está dentro do período de validade do agendamento fixo
            if (!dataAtual.isBefore(agendamentoFixo.getDataInicio()) &&
                (agendamentoFixo.getDataFim() == null || !dataAtual.isAfter(agendamentoFixo.getDataFim()))) {
                
                // Verifica se deve gerar ocorrência para esta data segundo a regra de repetição
                if (deveGerarOcorrencia(agendamentoFixo, dataAtual)) {
                    // Verifica se já existe um agendamento para esta data/hora/profissional
                    boolean jaExiste = agendamentoRepository.existsByProfissionalAndDataAndHoraAndAgendamentoFixoId(
                        agendamentoFixo.getProfissional(),
                        dataAtual,
                        agendamentoFixo.getHora(),
                        agendamentoFixo.getId()
                    );
                    
                    if (!jaExiste) {
                        // Verifica se o horário não está bloqueado
                        if (!isHorarioBloqueado(agendamentoFixo.getProfissional(), dataAtual, agendamentoFixo.getHora())) {
                            criarAgendamentoAPartirDeFixo(agendamentoFixo, dataAtual);
                            ocorrenciasCriadas++;
                        } else {
                            logger.debug("⏭️ [SCHEDULER] Horário bloqueado para data {} - ocorrência não criada", dataAtual);
                        }
                    }
                }
            }
            
            dataAtual = dataAtual.plusDays(1);
        }
        
        if (ocorrenciasCriadas > 0) {
            logger.info("✨ [SCHEDULER] Criadas {} novas ocorrências para agendamento fixo ID {} ({})",
                ocorrenciasCriadas, agendamentoFixo.getId(), agendamentoFixo.getCliente().getNome());
        }
        
        return ocorrenciasCriadas;
    }

    /**
     * Determina se deve gerar uma ocorrência para determinada data segundo a regra de repetição
     */
    private boolean deveGerarOcorrencia(AgendamentoFixo agendamentoFixo, LocalDate data) {
        switch (agendamentoFixo.getTipoRepeticao()) {
            case DIARIA:
                // Verifica se a data bate com o intervalo de repetição
                long diasDesdeInicioD = data.toEpochDay() - agendamentoFixo.getDataInicio().toEpochDay();
                return diasDesdeInicioD >= 0 && 
                       diasDesdeInicioD % agendamentoFixo.getIntervaloRepeticao() == 0;
                
            case SEMANAL:
                // Verifica se o dia da semana está no bitmask
                int diaDaSemana = data.getDayOfWeek().getValue() % 7 + 1; // 1=Dom, 7=Sáb
                boolean diaCorreto = (agendamentoFixo.getValorRepeticao() & (1 << (diaDaSemana - 1))) != 0;
                
                // Se tem intervalo > 1, verifica se é a semana correta
                if (diaCorreto && agendamentoFixo.getIntervaloRepeticao() > 1) {
                    long diasDesdeInicio = data.toEpochDay() - agendamentoFixo.getDataInicio().toEpochDay();
                    long semanasDesdeInicio = diasDesdeInicio / 7;
                    return semanasDesdeInicio % agendamentoFixo.getIntervaloRepeticao() == 0;
                }
                return diaCorreto;
                
            case QUINZENAL:
                // A cada 15 dias desde a data de início
                long diasDesdeInicioQ = data.toEpochDay() - agendamentoFixo.getDataInicio().toEpochDay();
                return diasDesdeInicioQ >= 0 && diasDesdeInicioQ % 15 == 0;
                
            case MENSAL:
                // Verifica se é o dia do mês correto
                int diaDoMes = data.getDayOfMonth();
                boolean mesCorreto = true;
                
                // Se tem intervalo > 1, verifica se é o mês correto
                if (agendamentoFixo.getIntervaloRepeticao() > 1) {
                    long mesesDesdeInicio = (data.getYear() - agendamentoFixo.getDataInicio().getYear()) * 12 +
                                           (data.getMonthValue() - agendamentoFixo.getDataInicio().getMonthValue());
                    mesCorreto = mesesDesdeInicio % agendamentoFixo.getIntervaloRepeticao() == 0;
                }
                
                if (!mesCorreto) return false;
                
                // Último dia do mês
                if (agendamentoFixo.getValorRepeticao() == -1) {
                    return diaDoMes == data.lengthOfMonth();
                }
                
                // Dia específico do mês (pode não existir em alguns meses, ex: 31)
                return agendamentoFixo.getDiaDoMes() == diaDoMes;
                
            default:
                return false;
        }
    }

    /**
     * Verifica se o horário está bloqueado para o profissional na data informada
     */
    private boolean isHorarioBloqueado(Profissional profissional, LocalDate data, LocalTime hora) {
        List<BloqueioAgenda> bloqueios = bloqueioRepository.findByProfissionalAndData(profissional, data);
        
        for (BloqueioAgenda bloqueio : bloqueios) {
            if (bloqueio.isDiaTodo()) {
                return true;
            }
            
            if (!hora.isBefore(bloqueio.getHoraInicio()) && !hora.isAfter(bloqueio.getHoraFim())) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Cria um agendamento concreto a partir de um agendamento fixo
     */
    private void criarAgendamentoAPartirDeFixo(AgendamentoFixo agendamentoFixo, LocalDate data) {
        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(agendamentoFixo.getCliente());
        agendamento.setProfissional(agendamentoFixo.getProfissional());
        agendamento.setServico(agendamentoFixo.getServico());
        agendamento.setData(data);
        agendamento.setHora(agendamentoFixo.getHora());
        agendamento.setObservacao(agendamentoFixo.getObservacao());
        agendamento.setAgendamentoFixoId(agendamentoFixo.getId());
        
        agendamentoRepository.save(agendamento);
        
        logger.debug("📅 [SCHEDULER] Criado agendamento para {} em {} às {}",
            agendamentoFixo.getCliente().getNome(), data, agendamentoFixo.getHora());
    }

    /**
     * Método público para forçar a geração manual (útil para testes e manutenção)
     */
    @Transactional
    public void forcarGeracaoManual() {
        logger.info("🔧 [MANUAL] Forçando geração manual de ocorrências futuras...");
        gerarOcorrenciasFuturas();
    }
}
