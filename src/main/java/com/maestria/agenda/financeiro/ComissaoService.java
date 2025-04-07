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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ComissaoService {

    private final AgendamentoRepository agendamentoRepository;
    private final AgendamentoFixoRepository agendamentoFixoRepository;
    private final ProfissionalRepository profissionalRepository;
    private final Logger logger = LoggerFactory.getLogger(ComissaoService.class);
    
    @Value("${comissao.percentual}")
    private double comissaoPercentual;

    public ComissaoService(AgendamentoRepository agendamentoRepository,
            AgendamentoFixoRepository agendamentoFixoRepository,
            ProfissionalRepository profissionalRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.agendamentoFixoRepository = agendamentoFixoRepository;
        this.profissionalRepository = profissionalRepository;
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
        
        // Usar o método do repositório que já filtra agendamentos normais (não derivados de fixos)
        List<Agendamento> agendamentosNormais = agendamentoRepository
                .findByProfissionalIdAndDataBetweenAndAgendamentoFixoIdIsNull(profissionalId, inicio, fim);
        
        logger.info("Encontrados {} agendamentos normais (não fixos)", agendamentosNormais.size());
        
        // Calcular valor total e desconto de taxa para cada agendamento
        for (Agendamento agendamento : agendamentosNormais) {
            if (agendamento.getServico() != null && agendamento.getServico().getValor() != null) {
                double valorServico = agendamento.getServico().getValor();
                
                // Taxa conforme forma de pagamento
                double taxa = 0.0;
                if (agendamento.getFormaPagamento() != null) {
                    taxa = agendamento.getFormaPagamento().getTaxa();
                }
                
                // Desconto devido à taxa
                double descontoTaxa = valorServico * (taxa / 100.0);
                
                valorTotal += valorServico;
                descontoTaxaTotal += descontoTaxa;
                
                logger.debug("Agendamento normal ID {}: valor {}, taxa {}%, desconto {}", 
                        agendamento.getId(), valorServico, taxa, descontoTaxa);
            }
        }
        
        // Calcular comissão bruta (baseada no valor total)
        double comissaoBruta = valorTotal * (comissaoPercentual / 100.0);
        
        logger.info("Comissão de agendamentos normais: valor total {}, comissão {}%, bruto {}, desconto {}", 
                valorTotal, comissaoPercentual, comissaoBruta, descontoTaxaTotal);
                
        return new ResultadoComissao(valorTotal, comissaoBruta, descontoTaxaTotal);
    }
    
    /**
     * Calcula a comissão para agendamentos fixos
     */
    private ResultadoComissao calcularComissaoAgendamentosFixos(Profissional profissional, LocalDate inicio, LocalDate fim) {
        logger.info("Calculando comissão de agendamentos fixos para {} entre {} e {}", 
                profissional.getNome(), inicio, fim);
        
        double valorTotal = 0.0;
        double descontoTaxaTotal = 0.0;
        
        // Buscar agendamentos fixos do profissional
        List<AgendamentoFixo> agendamentosFixos = agendamentoFixoRepository.findByProfissional(profissional);
        logger.info("Encontrados {} agendamentos fixos", agendamentosFixos.size());
        
        // Para cada agendamento fixo, calcular ocorrências no período
        for (AgendamentoFixo agendamentoFixo : agendamentosFixos) {
            if (agendamentoFixo.getServico() != null && agendamentoFixo.getServico().getValor() != null) {
                int ocorrencias = calcularDiasExecutadosNoPeriodo(agendamentoFixo, inicio, fim);
                
                if (ocorrencias > 0) {
                    double valorServico = agendamentoFixo.getServico().getValor();
                    double valorTotalServico = valorServico * ocorrencias;
                    
                    // Taxa conforme forma de pagamento
                    double taxa = 0.0;
                    
                    // Adicionar logs detalhados para depurar o problema
                    String formaPagamento = agendamentoFixo.getFormaPagamento();
                    logger.info("DEBUG - AgendamentoFixo ID {}: forma pagamento = '{}', tipo = {}", 
                            agendamentoFixo.getId(), formaPagamento, 
                            formaPagamento != null ? formaPagamento.getClass().getName() : "null");
                    
                    PagamentoTipo pagamentoTipo = null;
                    if (formaPagamento != null && !formaPagamento.trim().isEmpty()) {
                        // Usar método melhorado para converter a string em PagamentoTipo
                        pagamentoTipo = converterParaPagamentoTipo(formaPagamento);
                        
                        if (pagamentoTipo != null) {
                            taxa = pagamentoTipo.getTaxa();
                            logger.info("DEBUG - Forma de pagamento '{}' reconhecida como {}, taxa = {}%", 
                                    formaPagamento, pagamentoTipo, taxa);
                        } else {
                            logger.warn("DEBUG - ⚠️ Forma de pagamento não reconhecida: '{}'", formaPagamento);
                        }
                    } else {
                        logger.warn("DEBUG - ⚠️ Forma de pagamento nula ou vazia para agendamento fixo ID {}", 
                                agendamentoFixo.getId());
                    }
                    
                    // Desconto devido à taxa
                    double descontoTaxa = valorTotalServico * (taxa / 100.0);
                    logger.info("DEBUG - Valor serviço = {}, desconto = {}", valorTotalServico, descontoTaxa);
                    
                    valorTotal += valorTotalServico;
                    descontoTaxaTotal += descontoTaxa;
                    
                    logger.debug("Agendamento fixo ID {}: {} ocorrências x {} = {}, taxa {}%, desconto {}", 
                            agendamentoFixo.getId(), ocorrencias, valorServico, 
                            valorTotalServico, taxa, descontoTaxa);
                }
            }
        }
        
        // Calcular comissão bruta (baseada no valor total)
        double comissaoBruta = valorTotal * (comissaoPercentual / 100.0);
        
        logger.info("Comissão de agendamentos fixos: valor total {}, comissão {}%, bruto {}, desconto {}", 
                valorTotal, comissaoPercentual, comissaoBruta, descontoTaxaTotal);
                
        return new ResultadoComissao(valorTotal, comissaoBruta, descontoTaxaTotal);
    }
    
    /**
     * Método auxiliar para converter string em PagamentoTipo de forma mais robusta
     */
    private PagamentoTipo converterParaPagamentoTipo(String formaPagamento) {
        if (formaPagamento == null || formaPagamento.trim().isEmpty()) {
            return null;
        }
        
        String pgto = formaPagamento.trim().toUpperCase();
        
        try {
            // Tenta conversão direta
            return PagamentoTipo.valueOf(pgto);
        } catch (IllegalArgumentException e) {
            // Normaliza valores com variações comuns
            if (pgto.equals("DÉBITO") || pgto.equals("DEBITO")) {
                return PagamentoTipo.DEBITO;
            }
            if (pgto.equals("PIX") || pgto.contains("PIX")) {
                return PagamentoTipo.PIX;
            }
            if (pgto.equals("DINHEIRO") || pgto.contains("DINHEIRO") || pgto.equals("CASH")) {
                return PagamentoTipo.DINHEIRO;
            }
            
            // Tratamento para variações de crédito
            if (pgto.contains("CREDITO") || pgto.contains("CRÉDITO")) {
                if (pgto.contains("1") || pgto.contains("1X")) {
                    return PagamentoTipo.CREDITO_1X;
                }
                if (pgto.contains("2") || pgto.contains("2X")) {
                    return PagamentoTipo.CREDITO_2X;
                }
                if (pgto.contains("3") || pgto.contains("3X")) {
                    return PagamentoTipo.CREDITO_3X;
                }
                if (pgto.contains("4") || pgto.contains("4X")) {
                    return PagamentoTipo.CREDITO_4X;
                }
                if (pgto.contains("5") || pgto.contains("5X")) {
                    return PagamentoTipo.CREDITO_5X;
                }
                if (pgto.contains("6") || pgto.contains("6X")) {
                    return PagamentoTipo.CREDITO_6X;
                }
                if (pgto.contains("7") || pgto.contains("7X")) {
                    return PagamentoTipo.CREDITO_7X;
                }
                if (pgto.contains("8") || pgto.contains("8X")) {
                    return PagamentoTipo.CREDITO_8X;
                }
                if (pgto.contains("9") || pgto.contains("9X")) {
                    return PagamentoTipo.CREDITO_9X;
                }
                if (pgto.contains("10") || pgto.contains("10X")) {
                    return PagamentoTipo.CREDITO_10X;
                }
                
                // Se não identificou o número de parcelas, assume 1X
                return PagamentoTipo.CREDITO_1X;
            }
            
            logger.warn("⚠️ Forma de pagamento não reconhecida após normalização: '{}'", pgto);
            return null;
        }
    }
    
    /**
     * Calcula a comissão para um profissional específico em um período determinado.
     * Combina os resultados de agendamentos normais e fixos.
     */
    public ComissaoResponseDTO calcularComissaoPorPeriodo(Long profissionalId, LocalDate inicio, LocalDate fim) {
        try {
            Profissional profissional = profissionalRepository.findById(profissionalId)
                    .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));
            
            logger.info("Calculando comissão total para {} entre {} e {}", 
                    profissional.getNome(), inicio, fim);
            
            // 1. Calcular comissão de agendamentos normais
            ResultadoComissao resultadoNormal = calcularComissaoAgendamentosNormais(profissionalId, inicio, fim);
            
            // 2. Calcular comissão de agendamentos fixos
            ResultadoComissao resultadoFixo = calcularComissaoAgendamentosFixos(profissional, inicio, fim);
            
            // 3. Combinar os resultados
            double valorTotalServicos = resultadoNormal.valorTotalServicos + resultadoFixo.valorTotalServicos;
            double comissaoTotal = resultadoNormal.valorComissao + resultadoFixo.valorComissao;
            double descontoTaxaTotal = resultadoNormal.valorDescontoTaxa + resultadoFixo.valorDescontoTaxa;
            double comissaoLiquida = comissaoTotal - descontoTaxaTotal; // Corrigido para usar comissaoTotal - descontoTaxaTotal
            
            logger.info("RESUMO DA COMISSÃO DE {}", profissional.getNome());
            logger.info("Comissão de agendamentos normais: {} bruto, {} líquido, {} desconto", 
                    resultadoNormal.valorComissao, resultadoNormal.valorComissaoLiquida, resultadoNormal.valorDescontoTaxa);
            logger.info("Comissão de agendamentos fixos: {} bruto, {} líquido, {} desconto", 
                    resultadoFixo.valorComissao, resultadoFixo.valorComissaoLiquida, resultadoFixo.valorDescontoTaxa);
            logger.info("Comissão total: {} bruto, {} líquido, {} desconto", 
                    comissaoTotal, comissaoLiquida, descontoTaxaTotal);
            
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
                        true);
        } catch (Exception e) {
            logger.error("❌ Erro ao calcular comissão: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao calcular comissão: " + e.getMessage());
        }
    }
    
    /**
     * Calcula quantas vezes um agendamento fixo foi executado dentro de um período
     */
    private int calcularDiasExecutadosNoPeriodo(AgendamentoFixo agendamento, LocalDate inicio, LocalDate fim) {
        int diasExecutados = 0;
        
        // Ajustar para considerar apenas datas dentro do período especificado
        LocalDate dataInicio = agendamento.getDataInicio().isBefore(inicio) ? inicio : agendamento.getDataInicio();
        LocalDate dataFim = (agendamento.getDataFim() == null || agendamento.getDataFim().isAfter(fim))
                ? fim : agendamento.getDataFim();
                
        if (dataInicio.isAfter(dataFim)) {
            return 0;
        }
        
        LocalDate dataAtual = dataInicio;
        while (!dataAtual.isAfter(dataFim)) {
            boolean gerarOcorrencia = false;
            
            switch (agendamento.getTipoRepeticao()) {
                case DIARIA:
                    long diasDesdeInicio = dataAtual.toEpochDay() - agendamento.getDataInicio().toEpochDay();
                    gerarOcorrencia = diasDesdeInicio % agendamento.getIntervaloRepeticao() == 0;
                    break;
                    
                case SEMANAL:
                    int diaDaSemana = dataAtual.getDayOfWeek().getValue() % 7 + 1; // 1=domingo, 2=segunda
                    gerarOcorrencia = (agendamento.getValorRepeticao() & (1 << (diaDaSemana - 1))) != 0;
                    break;
                    
                case MENSAL:
                    if (agendamento.getValorRepeticao() == -1) {
                        // Último dia do mês
                        gerarOcorrencia = dataAtual.getDayOfMonth() == dataAtual.lengthOfMonth();
                    } else if (agendamento.getDiaDoMes() != null) {
                        // Dia específico do mês
                        gerarOcorrencia = agendamento.getDiaDoMes() == dataAtual.getDayOfMonth();
                    } else {
                        gerarOcorrencia = agendamento.getValorRepeticao() == dataAtual.getDayOfMonth();
                    }
                    break;
                    
                case QUINZENAL:
                    long diasDesdeInicioQ = dataAtual.toEpochDay() - agendamento.getDataInicio().toEpochDay();
                    gerarOcorrencia = diasDesdeInicioQ % 15 == 0;
                    break;
                    
                default:
                    break;
            }
            
            if (gerarOcorrencia) {
                diasExecutados++;
            }
            
            dataAtual = dataAtual.plusDays(1);
        }
        
        return diasExecutados;
    }
    
    /**
     * Lista todas as comissões para todos os profissionais em um período específico
     */
    public List<ComissaoResponseDTO> listarTodasComissoesNoPeriodo(LocalDate inicio, LocalDate fim) {
        try {
            List<Profissional> profissionais = profissionalRepository.findAll();
            List<ComissaoResponseDTO> comissoes = new ArrayList<>();
    
            for (Profissional profissional : profissionais) {
                try {
                    ComissaoResponseDTO comissao = calcularComissaoPorPeriodo(profissional.getId(), inicio, fim);
                    comissoes.add(comissao);
                } catch (Exception e) {
                    logger.error("❌ Erro ao calcular comissão para profissional {}: {}",
                            profissional.getId(), e.getMessage());
                }
            }
    
            return comissoes;
        } catch (Exception e) {
            logger.error("❌ Erro ao listar comissões no período", e);
            throw new RuntimeException("Erro ao listar comissões no período: " + e.getMessage());
        }
    }
    
    /**
     * Lista todas as comissões para o mês atual (método legado)
     */
    @Deprecated
    public List<ComissaoResponseDTO> listarComissoes() {
        LocalDate inicio = LocalDate.now().withDayOfMonth(1);
        LocalDate fim = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        return listarTodasComissoesNoPeriodo(inicio, fim);
    }
}