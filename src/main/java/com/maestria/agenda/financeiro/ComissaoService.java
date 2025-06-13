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
import java.util.ArrayList;
import java.util.stream.Collectors;

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
            
            // Buscar todas as comissões do período
            List<ComissaoPagamento> comissoes = comissaoPagamentoRepository.findByProfissionalIdAndPeriodo(profissionalId, inicio, fim);
            List<ComissaoIndividualDTO> comissoesIndividuais = comissoes.stream()
                .map(comissao -> new ComissaoIndividualDTO(
                    comissao.getId(),
                    comissao.getAgendamentoId(),
                    comissao.getDataPagamento(),
                    comissao.getValorComissao(),
                    comissao.getStatus().toString(),
                    comissao.getPaid()
                ))
                .collect(Collectors.toList());
            
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
                valorJaPago,
                comissoesIndividuais,
                comissoes);
        } catch (Exception e) {
            logger.error("❌ Erro ao calcular comissão: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao calcular comissão: " + e.getMessage());
        }
    }
    
    /**
     * Registra um pagamento de comissão para um período específico
     */
    public ComissaoResponseDTO registrarPagamentoComissao(Long profissionalId, LocalDate dataPagamento, 
            Double valorPago, String observacao, LocalDate periodoInicio, LocalDate periodoFim) {
        logger.info("💰 Registrando pagamento de comissão para profissional {} no valor de {} em {}", 
            profissionalId, valorPago, dataPagamento);
            
        Profissional profissional = profissionalRepository.findById(profissionalId)
            .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));
        
        // Validar valor do pagamento
        if (valorPago <= 0) {
            throw new RuntimeException("O valor do pagamento deve ser maior que zero");
        }
        
        // Verificar se já existe pagamento no período
        List<ComissaoPagamento> pagamentosExistentes = comissaoPagamentoRepository
            .findByProfissionalIdAndPeriodo(profissionalId, periodoInicio, periodoFim);
            
        for (ComissaoPagamento pagamento : pagamentosExistentes) {
            if (pagamento.getStatus() == ComissaoPagamento.StatusPagamento.PAGO && 
                pagamento.getValorPago() > 0) {
                throw new RuntimeException("Já existe um pagamento registrado para este período");
            }
        }
        
        // Calcular a comissão do período
        ComissaoResponseDTO comissao = calcularComissaoPorPeriodo(profissionalId, periodoInicio, periodoFim);
        
        // Verificar se o valor pago é válido
        if (valorPago > comissao.getValorPendente()) {
            throw new RuntimeException("Valor pago não pode ser maior que o valor pendente");
        }
        
        // Criar o registro de pagamento
        ComissaoPagamento pagamento = new ComissaoPagamento(
            profissionalId,
            null, // agendamentoId será null para pagamentos gerais
            dataPagamento,
            valorPago,
            observacao,
            periodoInicio,
            periodoFim
        );
        
        // Definir o valor da comissão como o valor pago e garantir que paid seja true
        pagamento.setValorComissao(valorPago);
        pagamento.setPaid(true);
        
        // Salvar o pagamento
        comissaoPagamentoRepository.save(pagamento);
        
        // Recalcular a comissão para retornar os valores atualizados
        return calcularComissaoPorPeriodo(profissionalId, periodoInicio, periodoFim);
    }

    /**
     * Calcula a comissão pendente para um profissional em um período específico
     */
    public ComissaoResponseDTO calcularComissaoPendente(Long profissionalId, LocalDate inicio, LocalDate fim) {
        ComissaoResponseDTO comissao = calcularComissaoPorPeriodo(profissionalId, inicio, fim);
        return comissao;
    }
    
    /**
     * Lista todos os pagamentos de comissão de um profissional em um período
     */
    public List<ComissaoPagamento> listarPagamentosPorPeriodo(Long profissionalId, LocalDate inicio, LocalDate fim) {
        return comissaoPagamentoRepository.findByProfissionalIdAndPeriodo(profissionalId, inicio, fim);
    }

    /**
     * Cancela um pagamento de comissão
     */
    public ComissaoResponseDTO cancelarPagamentoComissao(Long pagamentoId) {
        logger.info("❌ Cancelando pagamento de comissão ID: {}", pagamentoId);
        
        ComissaoPagamento pagamento = comissaoPagamentoRepository.findById(pagamentoId)
            .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));
            
        if (pagamento.getStatus() == ComissaoPagamento.StatusPagamento.CANCELADO) {
            throw new RuntimeException("Este pagamento já está cancelado");
        }
        
        // Cancelar o pagamento
        pagamento.setStatus(ComissaoPagamento.StatusPagamento.CANCELADO);
        comissaoPagamentoRepository.save(pagamento);
        
        // Recalcular a comissão para retornar os valores atualizados
        return calcularComissaoPorPeriodo(
            pagamento.getProfissionalId(),
            pagamento.getPeriodoInicio(),
            pagamento.getPeriodoFim()
        );
    }

    /**
     * Limpa pagamentos inválidos (zerados) de um profissional em um período
     */
    public List<ComissaoResponseDTO> limparPagamentosInvalidos(Long profissionalId, LocalDate inicio, LocalDate fim) {
        logger.info("🧹 Limpando pagamentos inválidos do profissional {} entre {} e {}", 
            profissionalId, inicio, fim);
            
        List<ComissaoPagamento> pagamentos = comissaoPagamentoRepository
            .findByProfissionalIdAndPeriodo(profissionalId, inicio, fim);
            
        List<ComissaoResponseDTO> resultados = new ArrayList<>();
        
        for (ComissaoPagamento pagamento : pagamentos) {
            if (pagamento.getValorPago() == 0 || pagamento.getValorPago() == null) {
                try {
                    ComissaoResponseDTO comissao = cancelarPagamentoComissao(pagamento.getId());
                    resultados.add(comissao);
                } catch (Exception e) {
                    logger.error("❌ Erro ao cancelar pagamento inválido {}: {}", 
                        pagamento.getId(), e.getMessage());
                }
            }
        }
        
        return resultados;
    }

    /**
     * Paga as comissões de um período específico
     */
    public ComissaoResponseDTO pagarComissoesPorPeriodo(Long profissionalId, LocalDate dataPagamento, 
            LocalDate periodoInicio, LocalDate periodoFim, Double valorPago, String observacao) {
        logger.info("💰 Registrando pagamento de comissões para profissional {} no valor de {} em {}", 
            profissionalId, valorPago, dataPagamento);
            
        Profissional profissional = profissionalRepository.findById(profissionalId)
            .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));
        
        // Validar valor do pagamento
        if (valorPago <= 0) {
            throw new RuntimeException("O valor do pagamento deve ser maior que zero");
        }
        
        // Buscar todas as comissões do período que ainda não foram pagas
        List<ComissaoPagamento> comissoesPendentes = comissaoPagamentoRepository
            .findByProfissionalIdAndPeriodo(profissionalId, periodoInicio, periodoFim)
            .stream()
            .filter(c -> c.getStatus() == ComissaoPagamento.StatusPagamento.PAGO && !c.getPaid())
            .collect(Collectors.toList());
            
        if (comissoesPendentes.isEmpty()) {
            throw new RuntimeException("Não há comissões pendentes para o período informado");
        }
        
        // Calcular o valor total das comissões pendentes
        double valorTotalPendente = comissoesPendentes.stream()
            .mapToDouble(ComissaoPagamento::getValorComissao)
            .sum();
            
        // Verificar se o valor pago é válido
        if (valorPago > valorTotalPendente) {
            throw new RuntimeException("Valor pago não pode ser maior que o valor pendente");
        }
        
        // Distribuir o valor pago entre as comissões
        double valorRestante = valorPago;
        for (ComissaoPagamento comissao : comissoesPendentes) {
            if (valorRestante <= 0) break;
            
            double valorComissao = comissao.getValorComissao();
            if (valorRestante >= valorComissao) {
                // Paga a comissão inteira
                comissao.setValorPago(valorComissao);
                comissao.setPaid(true);
                valorRestante -= valorComissao;
            } else {
                // Paga parcialmente
                comissao.setValorPago(valorRestante);
                comissao.setPaid(false);
                valorRestante = 0;
            }
            
            comissao.setDataPagamento(dataPagamento);
            comissao.setObservacao(observacao);
            comissaoPagamentoRepository.save(comissao);
        }
        
        // Recalcular a comissão para retornar os valores atualizados
        return calcularComissaoPorPeriodo(profissionalId, periodoInicio, periodoFim);
    }

    /**
     * Cancela as comissões de um período específico
     */
    public ComissaoResponseDTO cancelarComissoesPorPeriodo(Long profissionalId, 
            LocalDate periodoInicio, LocalDate periodoFim) {
        logger.info("❌ Cancelando comissões do profissional {} no período de {} a {}", 
            profissionalId, periodoInicio, periodoFim);
            
        Profissional profissional = profissionalRepository.findById(profissionalId)
            .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));
        
        // Buscar todas as comissões do período que estão pagas
        List<ComissaoPagamento> comissoesPagas = comissaoPagamentoRepository
            .findByProfissionalIdAndPeriodo(profissionalId, periodoInicio, periodoFim)
            .stream()
            .filter(c -> c.getStatus() == ComissaoPagamento.StatusPagamento.PAGO && c.getPaid())
            .collect(Collectors.toList());
            
        if (comissoesPagas.isEmpty()) {
            throw new RuntimeException("Não há comissões pagas para o período informado");
        }
        
        // Cancelar cada comissão
        for (ComissaoPagamento comissao : comissoesPagas) {
            comissao.cancelarComissao();
            comissaoPagamentoRepository.save(comissao);
        }
        
        // Recalcular a comissão para retornar os valores atualizados
        return calcularComissaoPorPeriodo(profissionalId, periodoInicio, periodoFim);
    }
}
