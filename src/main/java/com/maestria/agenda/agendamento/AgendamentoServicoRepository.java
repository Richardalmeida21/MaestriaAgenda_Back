package com.maestria.agenda.agendamento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgendamentoServicoRepository extends JpaRepository<AgendamentoServico, Long> {
    
    List<AgendamentoServico> findByAgendamentoIdOrderByOrdem(Long agendamentoId);
    
    void deleteByAgendamentoId(Long agendamentoId);
}
