package com.maestria.agenda.profissional;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {

    // Retorna uma lista de profissionais com o nome fornecido
    List<Profissional> findAllByNome(String nome);
    Profissional findByLogin(String login);
}


