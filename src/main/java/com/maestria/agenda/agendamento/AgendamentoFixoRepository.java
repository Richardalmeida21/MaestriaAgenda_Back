package com.maestria.agenda.agendamento;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.maestria.agenda.profissional.Profissional;

public interface AgendamentoFixoRepository extends JpaRepository<AgendamentoFixo, Long> {

    // Busca agendamentos fixos pelo dia do mês (para compatibilidade)
    @Query("SELECT af FROM AgendamentoFixo af WHERE af.tipoRepeticao = 'MENSAL' AND af.valorRepeticao = :diaDoMes")
    List<AgendamentoFixo> findByDiaDoMes(int diaDoMes);

    @Query("SELECT af FROM AgendamentoFixo af WHERE af.tipoRepeticao = 'QUINZENAL' AND (af.dataFim IS NULL OR af.dataFim >= :hoje) AND af.dataInicio <= :hoje")
List<AgendamentoFixo> findActiveQuinzenalSchedules(@Param("hoje") LocalDate hoje);

    // Busca agendamentos fixos mensais pelo profissional e dia do mês
    @Query("SELECT af FROM AgendamentoFixo af WHERE af.profissional = :profissional AND af.tipoRepeticao = 'MENSAL' AND af.valorRepeticao = :diaDoMes")
    List<AgendamentoFixo> findByProfissionalAndDiaDoMes(Profissional profissional, int diaDoMes);
    
    // Busca agendamentos fixos diários ativos
    @Query("SELECT af FROM AgendamentoFixo af WHERE af.tipoRepeticao = 'DIARIA' AND " +
           "(af.dataFim IS NULL OR af.dataFim >= :hoje) AND af.dataInicio <= :hoje")
    List<AgendamentoFixo> findActiveDailySchedules(@Param("hoje") LocalDate hoje);

    // Busca agendamentos fixos semanais para um dia específico da semana (1-7, onde 1=domingo)
    @Query("SELECT af FROM AgendamentoFixo af WHERE af.tipoRepeticao = 'SEMANAL' AND " +
           "bitand(af.valorRepeticao, power(2, :diaDaSemana - 1)) > 0 AND " +
           "(af.dataFim IS NULL OR af.dataFim >= :hoje) AND af.dataInicio <= :hoje")
    List<AgendamentoFixo> findActiveWeeklySchedulesForDay(
            @Param("hoje") LocalDate hoje,
            @Param("diaDaSemana") int diaDaSemana);

    // Busca agendamentos fixos mensais para um dia específico do mês
    @Query("SELECT af FROM AgendamentoFixo af WHERE af.tipoRepeticao = 'MENSAL' AND " +
           "af.valorRepeticao = :diaDoMes AND " +
           "(af.dataFim IS NULL OR af.dataFim >= :hoje) AND af.dataInicio <= :hoje")
    List<AgendamentoFixo> findActiveMonthlySchedulesForDay(
            @Param("hoje") LocalDate hoje,
            @Param("diaDoMes") int diaDoMes);

    // Busca agendamentos fixos que ocorrem no último dia do mês
    @Query("SELECT af FROM AgendamentoFixo af WHERE af.tipoRepeticao = 'MENSAL' AND " +
           "af.valorRepeticao = -1 AND " +
           "(af.dataFim IS NULL OR af.dataFim >= :hoje) AND af.dataInicio <= :hoje")
    List<AgendamentoFixo> findActiveMonthlySchedulesForLastDay(@Param("hoje") LocalDate hoje);

    // Busca todos os agendamentos fixos ativos para um profissional
    @Query("SELECT af FROM AgendamentoFixo af WHERE af.profissional = :profissional AND " +
           "(af.dataFim IS NULL OR af.dataFim >= :hoje) AND af.dataInicio <= :hoje")
    List<AgendamentoFixo> findActiveSchedulesByProfissional(
            @Param("profissional") Profissional profissional,
            @Param("hoje") LocalDate hoje);

    // Busca todos os agendamentos fixos ativos para uma data específica
    @Query("SELECT af FROM AgendamentoFixo af WHERE " + 
           "(af.dataFim IS NULL OR af.dataFim >= :data) AND af.dataInicio <= :data")
    List<AgendamentoFixo> findActiveSchedulesForDate(@Param("data") LocalDate data);

    List<AgendamentoFixo> findByProfissional(Profissional profissional);

    List<AgendamentoFixo> findByProfissionalIdAndAtivoTrue(Long profissionalId);

    // Busca todos os agendamentos fixos ativos (para o scheduler)
    List<AgendamentoFixo> findByAtivoTrue();
}