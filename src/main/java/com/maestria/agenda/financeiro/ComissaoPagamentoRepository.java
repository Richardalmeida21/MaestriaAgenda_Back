package com.maestria.agenda.financeiro;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
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
        
    /**
     * Encontra registros de pagamento para um profissional que se sobreponham ao período especificado
     * Retorna pagamentos onde o início <= fim consultado E o fim >= início consultado
     */
    @Query("SELECT cp FROM ComissaoPagamento cp WHERE cp.profissionalId = :profissionalId " +
           "AND cp.paid = true " +
           "AND cp.periodoInicio <= :fim " +
           "AND cp.periodoFim >= :inicio " +
           "ORDER BY cp.periodoInicio")
    List<ComissaoPagamento> findPaidPeriodsByProfissionalIdWithOverlap(
        @Param("profissionalId") Long profissionalId,
        @Param("inicio") LocalDate inicio,
        @Param("fim") LocalDate fim);
}
