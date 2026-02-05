package com.maestria.agenda.profissional;

import com.maestria.agenda.servico.CategoriaServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComissaoProfissionalRepository extends JpaRepository<ComissaoProfissional, Long> {
    List<ComissaoProfissional> findByProfissionalId(Long profissionalId);

    Optional<ComissaoProfissional> findByProfissionalAndCategoria(Profissional profissional,
            CategoriaServico categoria);

    Optional<ComissaoProfissional> findByProfissionalIdAndCategoriaId(Long profissionalId, Long categoriaId);
}
