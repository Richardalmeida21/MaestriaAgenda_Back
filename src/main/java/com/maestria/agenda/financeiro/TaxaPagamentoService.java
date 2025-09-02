package com.maestria.agenda.financeiro;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaxaPagamentoService {

    private final TaxaPagamentoRepository taxaPagamentoRepository;
    private final Logger logger = LoggerFactory.getLogger(TaxaPagamentoService.class);

    public TaxaPagamentoService(TaxaPagamentoRepository taxaPagamentoRepository) {
        this.taxaPagamentoRepository = taxaPagamentoRepository;
    }

    /**
     * Obtém a taxa configurada para um tipo de pagamento
     * Se não existir configuração, retorna a taxa padrão baseada no tipo
     */
    public double obterTaxa(PagamentoTipo tipoPagamento) {
        Optional<TaxaPagamento> taxaConfig = taxaPagamentoRepository.findByTipoPagamentoAndAtivoTrue(tipoPagamento);
        
        if (taxaConfig.isPresent()) {
            logger.debug("Taxa configurada encontrada para {}: {}%", tipoPagamento, taxaConfig.get().getTaxa());
            return taxaConfig.get().getTaxa();
        }
        
        // Fallback para taxas padrão (valores anteriores)
        double taxaPadrao = obterTaxaPadrao(tipoPagamento);
        logger.debug("Usando taxa padrão para {}: {}%", tipoPagamento, taxaPadrao);
        return taxaPadrao;
    }

    /**
     * Retorna as taxas padrão (valores que estavam no enum antes)
     */
    private double obterTaxaPadrao(PagamentoTipo tipoPagamento) {
        return switch (tipoPagamento) {
            case CREDITO_1X -> 2.0;
            case CREDITO_2X -> 2.5;
            case CREDITO_3X -> 3.0;
            case CREDITO_4X -> 3.5;
            case CREDITO_5X -> 4.0;
            case CREDITO_6X -> 4.5;
            case CREDITO_7X, CREDITO_8X, CREDITO_9X, CREDITO_10X -> 5.0;
            case DEBITO -> 1.5;
            case PIX, DINHEIRO -> 0.0;
        };
    }

    /**
     * Configura ou atualiza a taxa para um tipo de pagamento
     */
    public TaxaPagamento configurarTaxa(PagamentoTipo tipoPagamento, Double taxa) {
        logger.info("Configurando taxa para {}: {}%", tipoPagamento, taxa);
        
        Optional<TaxaPagamento> existente = taxaPagamentoRepository.findByTipoPagamento(tipoPagamento);
        
        TaxaPagamento taxaConfig;
        if (existente.isPresent()) {
            // Atualiza existente
            taxaConfig = existente.get();
            taxaConfig.setTaxa(taxa);
            taxaConfig.setAtivo(true);
        } else {
            // Cria nova configuração
            taxaConfig = new TaxaPagamento(tipoPagamento, taxa);
        }
        
        return taxaPagamentoRepository.save(taxaConfig);
    }

    /**
     * Lista todas as configurações de taxa
     */
    public List<TaxaPagamento> listarTodasTaxas() {
        return taxaPagamentoRepository.findAll();
    }

    /**
     * Desativa uma configuração de taxa (volta para padrão)
     */
    public void desativarTaxa(PagamentoTipo tipoPagamento) {
        logger.info("Desativando configuração de taxa para {}", tipoPagamento);
        
        Optional<TaxaPagamento> existente = taxaPagamentoRepository.findByTipoPagamento(tipoPagamento);
        if (existente.isPresent()) {
            TaxaPagamento taxaConfig = existente.get();
            taxaConfig.setAtivo(false);
            taxaPagamentoRepository.save(taxaConfig);
        }
    }

    /**
     * Inicializa as taxas padrão no banco se não existirem
     */
    public void inicializarTaxasPadrao() {
        logger.info("Verificando e inicializando taxas padrão...");
        
        for (PagamentoTipo tipo : PagamentoTipo.values()) {
            if (!taxaPagamentoRepository.existsByTipoPagamento(tipo)) {
                double taxaPadrao = obterTaxaPadrao(tipo);
                TaxaPagamento novaConfig = new TaxaPagamento(tipo, taxaPadrao);
                taxaPagamentoRepository.save(novaConfig);
                logger.info("Taxa padrão criada: {} = {}%", tipo, taxaPadrao);
            }
        }
    }
}
