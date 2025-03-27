package com.maestria.agenda.agendamento;

import com.maestria.agenda.profissional.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AgendamentoFixoRepository extends JpaRepository<AgendamentoFixo, Long> {

    // Busca agendamentos fixos pelo dia do mês
    List<AgendamentoFixo> findByDiaDoMes(int diaDoMes);

    // Busca agendamentos fixos pelo profissional e dia do mês
    List<AgendamentoFixo> findByProfissionalAndDiaDoMes(Profissional profissional, int diaDoMes);
    List<AgendamentoFixo> findByProfissional(Profissional profissional);
}
