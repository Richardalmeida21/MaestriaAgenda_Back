package com.maestria.agenda.agendamento;

import com.maestria.agenda.cliente.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {


    List<Agendamento> findByCliente(Cliente cliente);
}
