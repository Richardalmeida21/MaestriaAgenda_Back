package com.maestria.agenda.agendamento;

import com.maestria.agenda.cliente.Cliente;
import com.maestria.agenda.profissional.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;  // Importando LocalDateTime

import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    
    List<Agendamento> findByCliente(Cliente cliente);
    
    List<Agendamento> findByProfissional(Profissional profissional);
    
    // Alteração para LocalDateTime no método
    List<Agendamento> findByDataBetween(LocalDateTime dataInicio, LocalDateTime dataFim);

}
