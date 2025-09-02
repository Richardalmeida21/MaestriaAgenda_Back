package com.maestria.agenda.financeiro;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaxaPagamentoRepository extends JpaRepository<TaxaPagamento, Long> {
    
    /**
     * Busca a taxa ativa para um tipo de pagamento específico
     */
    Optional<TaxaPagamento> findByTipoPagamentoAndAtivoTrue(PagamentoTipo tipoPagamento);
    
    /**
     * Busca uma taxa por tipo de pagamento (ativa ou inativa)
     */
    Optional<TaxaPagamento> findByTipoPagamento(PagamentoTipo tipoPagamento);
    
    /**
     * Verifica se existe uma taxa para o tipo de pagamento
     */
    boolean existsByTipoPagamento(PagamentoTipo tipoPagamento);
    
    /**
     * Query para obter a taxa de um tipo de pagamento específico
     * Se não existir, retorna 0
     */
    @Query("SELECT COALESCE(t.taxa, 0.0) FROM TaxaPagamento t WHERE t.tipoPagamento = :tipoPagamento AND t.ativo = true")
    Double findTaxaByTipoPagamento(PagamentoTipo tipoPagamento);
}
