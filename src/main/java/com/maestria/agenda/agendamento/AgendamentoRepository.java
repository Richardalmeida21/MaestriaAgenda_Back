package com.maestria.agenda.agendamento;

import com.maestria.agenda.cliente.Cliente;
import com.maestria.agenda.profissional.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    List<Agendamento> findByProfissionalAndDataBetween(Profissional profissional, LocalDate dataInicio, LocalDate dataFim);

    List<Agendamento> findByCliente(Cliente cliente);

    List<Agendamento> findByProfissional(Profissional profissional);

    // Buscar agendamentos por data
    List<Agendamento> findByData(LocalDate data);

    // Buscar agendamentos por profissional e data
    List<Agendamento> findByProfissionalAndData(Profissional profissional, LocalDate data);

    // Contar agendamentos por profissional
    @Query("SELECT p.nome, COUNT(a) FROM Agendamento a JOIN a.profissional p GROUP BY p.nome")
    List<Object[]> countAgendamentosPorProfissional();

    // Contar agendamentos por cliente
    @Query("SELECT c.nome, COUNT(a) FROM Agendamento a JOIN a.cliente c GROUP BY c.nome")
    List<Object[]> countAgendamentosPorCliente();

    // Contar agendamentos por data
    @Query("SELECT a.data, COUNT(a) FROM Agendamento a GROUP BY a.data ORDER BY a.data")
    List<Object[]> countAgendamentosPorData();

    // Contar agendamentos por intervalo de datas
    long countByDataBetween(LocalDate dataInicio, LocalDate dataFim);

    // Contar agendamentos por profissional no intervalo de datas
    @Query("SELECT p.nome, COUNT(a) FROM Agendamento a JOIN a.profissional p WHERE a.data BETWEEN :dataInicio AND :dataFim GROUP BY p.nome")
    List<Object[]> countAgendamentosPorProfissionalBetween(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    // Contar agendamentos por cliente no intervalo de datas
    @Query("SELECT c.nome, COUNT(a) FROM Agendamento a JOIN a.cliente c WHERE a.data BETWEEN :dataInicio AND :dataFim GROUP BY c.nome")
    List<Object[]> countAgendamentosPorClienteBetween(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    // Contar agendamentos por data no intervalo de datas
    @Query("SELECT a.data, COUNT(a) FROM Agendamento a WHERE a.data BETWEEN :dataInicio AND :dataFim GROUP BY a.data ORDER BY a.data")
    List<Object[]> countAgendamentosPorDataBetween(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    // Calcular comissão por profissional
    @Query("SELECT a.profissional, SUM(a.valor * :comissaoPercentual) FROM Agendamento a GROUP BY a.profissional")
    List<Object[]> calcularComissaoPorProfissional(@Param("comissaoPercentual") double comissaoPercentual);

    // Calcular comissão total por período
    @Query("SELECT SUM(a.valor * :comissaoPercentual) " +
            "FROM Agendamento a " +
            "WHERE a.profissional.id = :profissionalId " +
            "AND a.data BETWEEN :dataInicio AND :dataFim")
    Double calcularComissaoTotalPorPeriodo(@Param("profissionalId") Long profissionalId,
                                           @Param("dataInicio") LocalDate dataInicio,
                                           @Param("dataFim") LocalDate dataFim,
                                           @Param("comissaoPercentual") double comissaoPercentual);
}
