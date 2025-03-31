package com.maestria.agenda.servico;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServicoRepository extends JpaRepository<Servico, Long> {
    boolean existsByNome(String nome);
    Servico findByNome(String nome);
    List<Servico> findByValorLessThanEqual(Double valorMaximo);
}