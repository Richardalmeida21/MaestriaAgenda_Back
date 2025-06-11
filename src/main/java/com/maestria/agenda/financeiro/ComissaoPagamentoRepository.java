package com.maestria.agenda.financeiro;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ComissaoPagamentoRepository extends JpaRepository<ComissaoPagamento, Long> {
    
    /**
     * Encontra todos os pagamentos de comissão para um profissional em um período específico
     */
    @Query("SELECT cp FROM ComissaoPagamento cp WHERE cp.profissionalId = :profissionalId " +
           "AND cp.dataPagamento BETWEEN :inicio AND :fim " +
           "ORDER BY cp.dataPagamento")
    List<ComissaoPagamento> findByProfissionalIdAndPeriodo(
        @Param("profissionalId") Long profissionalId, 
        @Param("inicio") LocalDate inicio, 
        @Param("fim") LocalDate fim);
        
    /**
     * Calcula o valor total pago em comissões para um profissional em um período específico
     * Considera apenas pagamentos não cancelados
     */
    @Query("SELECT COALESCE(SUM(cp.valorPago), 0) FROM ComissaoPagamento cp " +
           "WHERE cp.profissionalId = :profissionalId " +
           "AND cp.dataPagamento BETWEEN :inicio AND :fim " +
           "AND cp.status = 'PAGO'")
    Double calcularValorTotalPagoNoPeriodo(
        @Param("profissionalId") Long profissionalId,
        @Param("inicio") LocalDate inicio,
        @Param("fim") LocalDate fim);
}
