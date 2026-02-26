package com.maestria.agenda.agendamento;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.maestria.agenda.cliente.Cliente;
import com.maestria.agenda.profissional.Profissional;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

        List<Agendamento> findByProfissionalAndDataBetween(Profissional profissional, LocalDate dataInicio,
                        LocalDate dataFim);

        List<Agendamento> findByProfissionalIdAndDataBetween(Long profissionalId, LocalDate inicio, LocalDate fim);

        @Query("SELECT EXTRACT(HOUR FROM a.hora) AS horario, COUNT(a) " +
                        "FROM Agendamento a " +
                        "WHERE a.data BETWEEN :dataInicio AND :dataFim " +
                        "GROUP BY EXTRACT(HOUR FROM a.hora) " +
                        "ORDER BY COUNT(a) DESC")
        List<Object[]> findHorariosMaisProcurados(@Param("dataInicio") LocalDate dataInicio,
                        @Param("dataFim") LocalDate dataFim);

        List<Agendamento> findByCliente(Cliente cliente);

        List<Agendamento> findByProfissional(Profissional profissional);

        // Buscar agendamentos por data
        List<Agendamento> findByData(LocalDate data);

        // Buscar agendamentos por profissional e data
        List<Agendamento> findByProfissionalAndData(Profissional profissional, LocalDate data);

        List<Agendamento> findByDataBetween(LocalDate dataInicio, LocalDate dataFim);

        @Query("SELECT MONTH(a.data) AS month, SUM(a.servico.valor) FROM Agendamento a WHERE a.data BETWEEN :dataInicio AND :dataFim AND a.pago = true GROUP BY MONTH(a.data) ORDER BY MONTH(a.data)")
        List<Object[]> groupRevenueByMonth(@Param("dataInicio") LocalDate dataInicio,
                        @Param("dataFim") LocalDate dataFim);

        @Query("SELECT s.nome, COUNT(a) AS totalAgendamentos " +
                        "FROM Agendamento a JOIN a.servico s " +
                        "WHERE a.data BETWEEN :dataInicio AND :dataFim AND a.pago = true " +
                        "GROUP BY s.nome " +
                        "ORDER BY totalAgendamentos DESC")
        List<Object[]> findServicosMaisAgendados(@Param("dataInicio") LocalDate dataInicio,
                        @Param("dataFim") LocalDate dataFim);

        // Queries otimizadas para Dashboard - apenas contagens
        @Query("SELECT COUNT(a) FROM Agendamento a WHERE a.data = :data")
        long countByData(@Param("data") LocalDate data);

        // Buscar próximos agendamentos com dados mínimos (apenas para exibição)
        // Nota: duracao não é um campo da entidade, é calculado dinamicamente
        @Query("SELECT a.id, a.cliente.nome, a.profissional.nome, a.servico.nome, a.data, a.hora " +
               "FROM Agendamento a " +
               "WHERE a.data >= :dataInicio " +
               "ORDER BY a.data ASC, a.hora ASC")
        List<Object[]> findNextAppointmentsMinimal(@Param("dataInicio") LocalDate dataInicio);

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
        List<Object[]> countAgendamentosPorProfissionalBetween(@Param("dataInicio") LocalDate dataInicio,
                        @Param("dataFim") LocalDate dataFim);

        // Contar agendamentos por cliente no intervalo de datas
        @Query("SELECT c.nome, COUNT(a) FROM Agendamento a JOIN a.cliente c WHERE a.data BETWEEN :dataInicio AND :dataFim GROUP BY c.nome")
        List<Object[]> countAgendamentosPorClienteBetween(@Param("dataInicio") LocalDate dataInicio,
                        @Param("dataFim") LocalDate dataFim);

        // Contar agendamentos por data no intervalo de datas
        @Query("SELECT a.data, COUNT(a) FROM Agendamento a WHERE a.data BETWEEN :dataInicio AND :dataFim GROUP BY a.data ORDER BY a.data")
        List<Object[]> countAgendamentosPorDataBetween(@Param("dataInicio") LocalDate dataInicio,
                        @Param("dataFim") LocalDate dataFim);

        // Calcular comissão por profissional (desconsiderando agendamentos gerados a
        // partir de fixos)
        @Query("SELECT a.profissional, SUM(a.servico.valor * :comissaoPercentual) " +
                        "FROM Agendamento a " +
                        "WHERE a.agendamentoFixoId IS NULL " +
                        "GROUP BY a.profissional")
        List<Object[]> calcularComissaoPorProfissional(@Param("comissaoPercentual") double comissaoPercentual);

        // Calcular comissão total por período (somente agendamentos manuais)
        @Query("SELECT SUM(a.servico.valor * :comissaoPercentual) " +
                        "FROM Agendamento a " +
                        "WHERE a.profissional.id = :profissionalId " +
                        "AND a.data BETWEEN :dataInicio AND :dataFim " +
                        "AND a.agendamentoFixoId IS NULL")
        Double calcularComissaoTotalPorPeriodo(@Param("profissionalId") Long profissionalId,
                        @Param("dataInicio") LocalDate dataInicio,
                        @Param("dataFim") LocalDate dataFim,
                        @Param("comissaoPercentual") double comissaoPercentual);

        // Calcular faturamento total por período (apenas agendamentos pagos)
        @Query("SELECT SUM(a.servico.valor) FROM Agendamento a WHERE a.data BETWEEN :dataInicio AND :dataFim AND a.pago = true")
        Double calcularFaturamentoTotalPorPeriodo(@Param("dataInicio") LocalDate dataInicio,
                        @Param("dataFim") LocalDate dataFim);

        // Contar serviços realizados por período (apenas agendamentos pagos)
        @Query("SELECT COUNT(a) FROM Agendamento a WHERE a.data BETWEEN :dataInicio AND :dataFim AND a.pago = true")
        Integer contarServicosRealizadosPorPeriodo(@Param("dataInicio") LocalDate dataInicio,
                        @Param("dataFim") LocalDate dataFim);

        // Contar novos clientes por período (apenas agendamentos pagos)
        @Query("SELECT COUNT(DISTINCT a.cliente) FROM Agendamento a WHERE a.data BETWEEN :dataInicio AND :dataFim AND a.pago = true")
        Integer contarNovosClientesPorPeriodo(@Param("dataInicio") LocalDate dataInicio,
                        @Param("dataFim") LocalDate dataFim);

        // Contar total de clientes por período (apenas agendamentos pagos)
        @Query("SELECT COUNT(DISTINCT a.cliente) FROM Agendamento a WHERE a.data BETWEEN :dataInicio AND :dataFim AND a.pago = true")
        Integer contarTotalDeClientesPorPeriodo(@Param("dataInicio") LocalDate dataInicio,
                        @Param("dataFim") LocalDate dataFim);

        List<Agendamento> findByProfissionalIdAndDataBetweenAndAgendamentoFixoIdIsNull(
                        Long profissionalId, LocalDate inicio, LocalDate fim);

        // Adicione este método ao AgendamentoRepository
        List<Agendamento> findByAgendamentoFixoId(Long agendamentoFixoId);

        List<Agendamento> findByAgendamentoFixoIdAndDataBetweenAndPagoTrue(Long agendamentoFixoId, LocalDate inicio,
                        LocalDate fim);

        // ============================================
        // QUERIES OTIMIZADAS PARA MÉTRICAS (EVITAR MEMÓRIA DO JAVA)
        // ============================================
        
        @Query("SELECT a.cliente.id, a.data FROM Agendamento a WHERE a.data BETWEEN :dataInicio AND :dataFim ORDER BY a.data ASC")
        List<Object[]> findClientIdsAndDatesBetween(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);


        // ============================================
        // QUERIES OTIMIZADAS COM FETCH JOIN
        // ============================================
        // Evita problema N+1 ao carregar cliente e servico em uma única query

        /**
         * Busca agendamentos com cliente e servico carregados (FETCH JOIN)
         * Otimizado para cálculo de comissões - evita N+1 queries
         */
        @Query("SELECT DISTINCT a FROM Agendamento a " +
                        "LEFT JOIN FETCH a.cliente " +
                        "LEFT JOIN FETCH a.servico " +
                        "WHERE a.profissional.id = :profissionalId " +
                        "AND a.data BETWEEN :inicio AND :fim " +
                        "ORDER BY a.data, a.hora")
        List<Agendamento> findByProfissionalComDetalhes(
                        @Param("profissionalId") Long profissionalId,
                        @Param("inicio") LocalDate inicio,
                        @Param("fim") LocalDate fim);

        /**
         * Busca agendamentos normais (não fixos) com detalhes carregados
         * Otimizado para cálculo de comissões
         */
        @Query("SELECT DISTINCT a FROM Agendamento a " +
                        "LEFT JOIN FETCH a.cliente " +
                        "LEFT JOIN FETCH a.servico " +
                        "WHERE a.profissional.id = :profissionalId " +
                        "AND a.data BETWEEN :inicio AND :fim " +
                        "AND a.agendamentoFixoId IS NULL " +
                        "ORDER BY a.data, a.hora")
        List<Agendamento> findAgendamentosNormaisComDetalhes(
                        @Param("profissionalId") Long profissionalId,
                        @Param("inicio") LocalDate inicio,
                        @Param("fim") LocalDate fim);

        /**
         * Busca agendamentos fixos com detalhes carregados
         * Otimizado para cálculo de comissões
         */
        @Query("SELECT DISTINCT a FROM Agendamento a " +
                        "LEFT JOIN FETCH a.cliente " +
                        "LEFT JOIN FETCH a.servico " +
                        "WHERE a.profissional.id = :profissionalId " +
                        "AND a.data BETWEEN :inicio AND :fim " +
                        "AND a.agendamentoFixoId IS NOT NULL " +
                        "ORDER BY a.data, a.hora")
        List<Agendamento> findAgendamentosFixosComDetalhes(
                        @Param("profissionalId") Long profissionalId,
                        @Param("inicio") LocalDate inicio,
                        @Param("fim") LocalDate fim);

        /**
         * Verifica se já existe um agendamento para evitar duplicação
         * Usado pelo scheduler de agendamentos fixos
         */
        boolean existsByProfissionalAndDataAndHoraAndAgendamentoFixoId(
                        Profissional profissional,
                        LocalDate data,
                        java.time.LocalTime hora,
                        Long agendamentoFixoId);
}