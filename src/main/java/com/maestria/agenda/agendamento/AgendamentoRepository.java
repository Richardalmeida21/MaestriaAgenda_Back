package com.maestria.agenda.agendamento;

import com.maestria.agenda.cliente.Cliente;
import com.maestria.agenda.profissional.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository <Agendamento, Long> {
    
    List<Agendamento> findByCliente(Cliente cliente);
    
    List<Agendamento> findByProfissional(Profissional profissional);
    
    // Novo método para buscar agendamentos por data
    List<Agendamento> findByData(LocalDate data);
    
    // Novo método para buscar agendamentos por profissional e data
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
}
