package com.maestria.agenda.financeiro;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface ComissaoPagamentoRepository extends JpaRepository<ComissaoPagamento, Long> {
    
    /**
     * Encontra o registro de pagamento para um profissional em um período específico
     */
    @Query("SELECT cp FROM ComissaoPagamento cp WHERE cp.profissionalId = :profissionalId " +
           "AND cp.periodoInicio = :inicio AND cp.periodoFim = :fim")
    Optional<ComissaoPagamento> findByProfissionalIdAndPeriodo(
        @Param("profissionalId") Long profissionalId, 
        @Param("inicio") LocalDate inicio, 
        @Param("fim") LocalDate fim);
}
