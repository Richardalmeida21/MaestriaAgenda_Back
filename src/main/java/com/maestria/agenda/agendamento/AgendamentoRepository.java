package com.maestria.agenda.agendamento;

import com.maestria.agenda.cliente.Cliente;
import com.maestria.agenda.profissional.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    
    List<Agendamento> findByCliente(Cliente cliente);
    
    List<Agendamento> findByProfissional(Profissional profissional);
    
    // Novo método para buscar agendamentos por data
    List<Agendamento> findByData(LocalDate data);
    
    // Novo método para buscar agendamentos por profissional e data
    List<Agendamento> findByProfissionalAndData(Profissional profissional, LocalDate data);
}