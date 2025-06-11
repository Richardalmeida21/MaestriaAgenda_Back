package com.maestria.agenda.financeiro;

import com.maestria.agenda.agendamento.Agendamento;
import com.maestria.agenda.agendamento.AgendamentoFixo;
import com.maestria.agenda.agendamento.AgendamentoFixoRepository;
import com.maestria.agenda.agendamento.AgendamentoRepository;
import com.maestria.agenda.profissional.Profissional;
import com.maestria.agenda.profissional.ProfissionalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Service
public class ComissaoService {

    private final AgendamentoRepository agendamentoRepository;
    private final AgendamentoFixoRepository agendamentoFixoRepository;
    private final ProfissionalRepository profissionalRepository;
    private final ComissaoPagamentoRepository comissaoPagamentoRepository;
    private final Logger logger = LoggerFactory.getLogger(ComissaoService.class);
    
    @Value("${comissao.percentual}")
    private double comissaoPercentual;

    public ComissaoService(AgendamentoRepository agendamentoRepository,
            AgendamentoFixoRepository agendamentoFixoRepository,
            ProfissionalRepository profissionalRepository,
            ComissaoPagamentoRepository comissaoPagamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.agendamentoFixoRepository = agendamentoFixoRepository;
        this.profissionalRepository = profissionalRepository;
        this.comissaoPagamentoRepository = comissaoPagamentoRepository;
    }
    
    /**
     * Classe auxiliar para armazenar os resultados do cálculo de comissão
     */
    private static class ResultadoComissao {
        double valorTotalServicos;
        double valorComissao;
        double valorDescontoTaxa;
        double valorComissaoLiquida;
        
        ResultadoComissao(double valorTotalServicos, double valorComissao, double valorDescontoTaxa) {
            this.valorTotalServicos = valorTotalServicos;
            this.valorComissao = valorComissao;
            this.valorDescontoTaxa = valorDescontoTaxa;
            this.valorComissaoLiquida = valorComissao - valorDescontoTaxa;
        }
    }
    
    /**
     * Calcula a comissão para agendamentos normais (não derivados de fixos)
     */
    private ResultadoComissao calcularComissaoAgendamentosNormais(Long profissionalId, LocalDate inicio, LocalDate fim) {
        logger.info("Calculando comissão de agendamentos normais para profissional {} entre {} e {}", 
                profissionalId, inicio, fim);
        
        double valorTotal = 0.0;
        double descontoTaxaTotal = 0.0;
        
        List<Agendamento> agendamentosNormais = agendamentoRepository
                .findByProfissionalIdAndDataBetweenAndAgendamentoFixoIdIsNull(profissionalId, inicio, fim);
        
        logger.info("Encontrados {} agendamentos normais (não fixos)", agendamentosNormais.size());
        
        for (Agendamento agendamento : agendamentosNormais) {
            if (agendamento.getPago() != null && agendamento.getPago() && 
                agendamento.getServico() != null && agendamento.getServico().getValor() != null) {
                
                double valorServico = agendamento.getServico().getValor();
                double taxa = 0.0;
                
                if (agendamento.getFormaPagamento() != null) {
                    taxa = agendamento.getFormaPagamento().getTaxa();
                }
                
                double descontoTaxa = valorServico * (taxa / 100.0);
                
                valorTotal += valorServico;
                descontoTaxaTotal += descontoTaxa;
                
                logger.debug("Agendamento normal ID {}: valor {}, taxa {}%, desconto {}", 
                        agendamento.getId(), valorServico, taxa, descontoTaxa);
            }
        }
        
        double comissaoBruta = valorTotal * (comissaoPercentual / 100.0);
        
        logger.info("Comissão de agendamentos normais: valor total {}, comissão {}%, bruto {}, desconto {}", 
                valorTotal, comissaoPercentual, comissaoBruta, descontoTaxaTotal);
                
        return new ResultadoComissao(valorTotal, comissaoBruta, descontoTaxaTotal);
    }
    
    /**
     * Calcula a comissão para agendamentos fixos
     */
    private ResultadoComissao calcularComissaoAgendamentosFixos(Long profissionalId, LocalDate inicio, LocalDate fim) {
        logger.info("Calculando comissão de agendamentos fixos para profissional {} entre {} e {}", 
                profissionalId, inicio, fim);
        
        double valorTotal = 0.0;
        double descontoTaxaTotal = 0.0;
        
        List<AgendamentoFixo> agendamentosFixos = agendamentoFixoRepository
                .findByProfissionalIdAndAtivoTrue(profissionalId);
        
        logger.info("Encontrados {} agendamentos fixos ativos", agendamentosFixos.size());
        
        for (AgendamentoFixo agendamentoFixo : agendamentosFixos) {
            List<Agendamento> agendamentosGerados = agendamentoRepository
                .findByAgendamentoFixoIdAndDataBetweenAndPagoTrue(agendamentoFixo.getId(), inicio, fim);
            
            if (!agendamentosGerados.isEmpty()) {
                double valorServico = agendamentoFixo.getServico().getValor();
                double valorTotalServico = valorServico * agendamentosGerados.size();
                double descontoTaxa = 0.0;
                
                for (Agendamento agendamento : agendamentosGerados) {
                    if (agendamento.getFormaPagamento() != null) {
                        double taxa = agendamento.getFormaPagamento().getTaxa();
                        descontoTaxa += valorServico * (taxa / 100.0);
                    }
                }
                
                valorTotal += valorTotalServico;
                descontoTaxaTotal += descontoTaxa;
                
                logger.debug("Agendamento fixo ID {}: {} ocorrências pagas x {} = {}, desconto {}", 
                        agendamentoFixo.getId(), agendamentosGerados.size(), valorServico, 
                        valorTotalServico, descontoTaxa);
            }
        }
        
        double comissaoBruta = valorTotal * (comissaoPercentual / 100.0);
        
        logger.info("Comissão de agendamentos fixos: valor total {}, comissão {}%, bruto {}, desconto {}", 
                valorTotal, comissaoPercentual, comissaoBruta, descontoTaxaTotal);
                
        return new ResultadoComissao(valorTotal, comissaoBruta, descontoTaxaTotal);
    }
    
    /**
     * Calcula a comissão para um profissional específico em um período determinado.
     * Combina os resultados de agendamentos normais e fixos.
     */
    public ComissaoResponseDTO calcularComissaoPorPeriodo(Long profissionalId, LocalDate inicio, LocalDate fim) {
        try {
            Profissional profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));
            
            // Calcular comissão de agendamentos normais
            ResultadoComissao resultadoNormal = calcularComissaoAgendamentosNormais(profissionalId, inicio, fim);
            
            // Calcular comissão de agendamentos fixos
            ResultadoComissao resultadoFixo = calcularComissaoAgendamentosFixos(profissionalId, inicio, fim);
            
            // Somar os resultados
            double comissaoTotal = resultadoNormal.valorComissao + resultadoFixo.valorComissao;
            double comissaoLiquida = resultadoNormal.valorComissaoLiquida + resultadoFixo.valorComissaoLiquida;
            double descontoTaxaTotal = resultadoNormal.valorDescontoTaxa + resultadoFixo.valorDescontoTaxa;
            
            // Calcular valor já pago no período
            double valorJaPago = comissaoPagamentoRepository.calcularValorTotalPagoNoPeriodo(profissionalId, inicio, fim);
            
            logger.info("Comissão de agendamentos normais: {} bruto, {} líquido, {} desconto", 
                    resultadoNormal.valorComissao, resultadoNormal.valorComissaoLiquida, resultadoNormal.valorDescontoTaxa);
            logger.info("Comissão de agendamentos fixos: {} bruto, {} líquido, {} desconto", 
                    resultadoFixo.valorComissao, resultadoFixo.valorComissaoLiquida, resultadoFixo.valorDescontoTaxa);
            logger.info("Comissão total: {} bruto, {} líquido, {} desconto, {} já pago", 
                    comissaoTotal, comissaoLiquida, descontoTaxaTotal, valorJaPago);
            
            return new ComissaoResponseDTO(
                profissional.getId(),
                profissional.getNome(),
                inicio,
                fim,
                comissaoTotal,
                comissaoLiquida,
                resultadoNormal.valorComissao,
                resultadoFixo.valorComissao,
                descontoTaxaTotal,
                valorJaPago);
        } catch (Exception e) {
            logger.error("❌ Erro ao calcular comissão: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao calcular comissão: " + e.getMessage());
        }
    }
    
    /**
     * Registra um pagamento de comissão
     */
    public ComissaoResponseDTO registrarPagamentoComissao(Long profissionalId, LocalDate dataPagamento, 
            Double valorPago, String observacao) {
        try {
            // Validar profissional
            Profissional profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));
            
            // Criar registro de pagamento
            LocalDate periodoInicio = dataPagamento.withDayOfMonth(1);
            LocalDate periodoFim = dataPagamento.withDayOfMonth(dataPagamento.lengthOfMonth());
            ComissaoPagamento pagamento = new ComissaoPagamento(
                profissionalId,
                dataPagamento,
                valorPago,
                observacao,
                periodoInicio,
                periodoFim
            );
            
            comissaoPagamentoRepository.save(pagamento);
            
            // Calcular comissão atualizada para o mês atual
            return calcularComissaoPorPeriodo(profissionalId, periodoInicio, periodoFim);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao registrar pagamento de comissão: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao registrar pagamento de comissão: " + e.getMessage());
        }
    }
    
    /**
     * Lista todos os pagamentos de comissão de um profissional em um período
     */
    public List<ComissaoPagamento> listarPagamentosPorPeriodo(Long profissionalId, LocalDate inicio, LocalDate fim) {
        return comissaoPagamentoRepository.findByProfissionalIdAndPeriodo(profissionalId, inicio, fim);
    }
}
