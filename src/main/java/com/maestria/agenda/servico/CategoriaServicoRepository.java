package com.maestria.agenda.servico;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaServicoRepository extends JpaRepository<CategoriaServico, Long> {
    boolean existsByNome(String nome);

    CategoriaServico findByNome(String nome);
}
