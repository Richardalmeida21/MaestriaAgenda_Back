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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        
        // Usar o método do repositório que já filtra agendamentos normais (não derivados de fixos)
        List<Agendamento> agendamentosNormais = agendamentoRepository
                .findByProfissionalIdAndDataBetweenAndAgendamentoFixoIdIsNull(profissionalId, inicio, fim);
        
        logger.info("Encontrados {} agendamentos normais (não fixos)", agendamentosNormais.size());
        
        // Calcular valor total e desconto de taxa para cada agendamento
        for (Agendamento agendamento : agendamentosNormais) {
            if (agendamento.getServico() != null && agendamento.getServico().getValor() != null) {
                double valorServico = agendamento.getServico().getValor();
                
                // Taxa conforme forma de pagamento (só se pago)
                double taxa = 0.0;
                if (agendamento.getPago() != null && agendamento.getPago() && agendamento.getFormaPagamento() != null) {
                    taxa = agendamento.getFormaPagamento().getTaxa();
                } else {
                    taxa = 0.0; // Se não foi dado baixa ainda, não desconta taxa
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
                    
                    // Taxa conforme forma de pagamento (fixos não têm baixa individual, taxa 0%)
                    double taxa = 0.0;
                    
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
     * Verifica se existe algum período de pagamento que se sobreponha ao período consultado
     */
    private boolean verificarSePeriodoPossuiPagamento(Long profissionalId, LocalDate inicio, LocalDate fim) {
        List<ComissaoPagamento> pagamentosComSobreposicao = comissaoPagamentoRepository
            .findPaidPeriodsByProfissionalIdWithOverlap(profissionalId, inicio, fim);
            
        if (!pagamentosComSobreposicao.isEmpty()) {
            for (ComissaoPagamento pagamento : pagamentosComSobreposicao) {
                // Verificar se o período está totalmente coberto
                if (pagamento.getPeriodoInicio().compareTo(inicio) <= 0 && 
                    pagamento.getPeriodoFim().compareTo(fim) >= 0) {
                    // O período consultado está completamente coberto por um pagamento existente
                    logger.info("Período {}-{} já está totalmente pago (período existente: {}-{})", 
                        inicio, fim, pagamento.getPeriodoInicio(), pagamento.getPeriodoFim());
                    return true;
                }
            }
            
            // Se não está totalmente coberto, mas há sobreposições, vamos logar para debug
            logger.info("Período {}-{} possui {} pagamentos com sobreposição, mas não está totalmente coberto", 
                inicio, fim, pagamentosComSobreposicao.size());
        }
        
        return false;
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
            double comissaoLiquida = comissaoTotal - descontoTaxaTotal;
            
            logger.info("RESUMO DA COMISSÃO DE {}", profissional.getNome());
            logger.info("Comissão de agendamentos normais: {} bruto, {} líquido, {} desconto", 
                    resultadoNormal.valorComissao, resultadoNormal.valorComissaoLiquida, resultadoNormal.valorDescontoTaxa);
            logger.info("Comissão de agendamentos fixos: {} bruto, {} líquido, {} desconto", 
                    resultadoFixo.valorComissao, resultadoFixo.valorComissaoLiquida, resultadoFixo.valorDescontoTaxa);
            logger.info("Comissão total: {} bruto, {} líquido, {} desconto", 
                    comissaoTotal, comissaoLiquida, descontoTaxaTotal);
            
            // Verificar se já existe registro de pagamento para este período e profissional
            boolean isPaid = false;
            
            // Verificar se existe um pagamento exatamente para este período
            Optional<ComissaoPagamento> pagamentoExistente = comissaoPagamentoRepository
                .findByProfissionalIdAndPeriodo(profissionalId, inicio, fim);
                
            if (pagamentoExistente.isPresent()) {
                isPaid = pagamentoExistente.get().getPaid();
            } else {
                // Se não existe pagamento exato para o período, verificar se já existe algum 
                // pagamento que engloba completamente este período
                isPaid = verificarSePeriodoPossuiPagamento(profissionalId, inicio, fim);
            }
            
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
                isPaid);
        } catch (Exception e) {
            logger.error("❌ Erro ao calcular comissão: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao calcular comissão: " + e.getMessage());
        }
    }
    
    /**
     * Atualiza o status de pagamento de uma comissão
     */
    public ComissaoResponseDTO atualizarStatusPagamento(Long profissionalId, LocalDate inicio, LocalDate fim, boolean paid) {
    try {
        // Verificar se o período já está coberto por pagamentos existentes quando tentando marcar como pago
        if (paid && verificarSePeriodoPossuiPagamento(profissionalId, inicio, fim)) {
            logger.warn("⚠️ Tentativa de pagar comissão que já está marcada como paga para profissional {} no período {} a {}", 
                profissionalId, inicio, fim);
            throw new RuntimeException("Este período já possui pagamento registrado. Para evitar duplicidade, a operação foi cancelada.");
        }
        
        // Buscar registro existente ou criar um novo
        Optional<ComissaoPagamento> pagamentoOptional = comissaoPagamentoRepository
            .findByProfissionalIdAndPeriodo(profissionalId, inicio, fim);
        
        ComissaoPagamento pagamento;
        
        if (pagamentoOptional.isPresent()) {
            pagamento = pagamentoOptional.get();
            
            // Verificação para evitar duplo pagamento
            if (paid && pagamento.getPaid()) {
                logger.warn("⚠️ Tentativa de pagar comissão já paga para profissional {} no período {} a {}", 
                    profissionalId, inicio, fim);
                throw new RuntimeException("Esta comissão já está marcada como paga.");
            }
            
            // Verificação para evitar reverter uma comissão não paga
            if (!paid && !pagamento.getPaid()) {
                logger.warn("⚠️ Tentativa de reverter comissão não paga para profissional {} no período {} a {}", 
                    profissionalId, inicio, fim);
                throw new RuntimeException("Esta comissão já está marcada como não paga.");
            }
            
        } else {
            // Criar novo registro se não existir
            logger.info("Criando novo registro de pagamento para profissional {} no período {} a {}", 
                profissionalId, inicio, fim);
            pagamento = new ComissaoPagamento(profissionalId, inicio, fim, 0.0, paid);
        }

        // Atualizar status de pagamento
        pagamento.setPaid(paid);
        if (paid) {
            pagamento.setDataPagamento(LocalDateTime.now()); // Define a data de pagamento
        } else {
            pagamento.setDataPagamento(null); // Remove a data de pagamento se marcado como não pago
        }
        comissaoPagamentoRepository.save(pagamento);

        // Calcular a comissão para pegar os valores corretos e atualizar o valor no registro
        ComissaoResponseDTO comissao = calcularComissaoPorPeriodo(profissionalId, inicio, fim);
        
        // Atualizar o valor da comissão no registro
        pagamento.setValorComissao(comissao.getComissaoLiquida());
        comissaoPagamentoRepository.save(pagamento);

        // Retornar comissão atualizada
        return new ComissaoResponseDTO(
            comissao.getProfissionalId(),
            comissao.getNomeProfissional(),
            comissao.getDataInicio(),
            comissao.getDataFim(),
            comissao.getComissaoTotal(),
            comissao.getComissaoLiquida(),
            comissao.getComissaoAgendamentosNormais(),
            comissao.getComissaoAgendamentosFixos(),
            comissao.getDescontoTaxa(),
            pagamento.getPaid()
        );
    } catch (RuntimeException e) {
        // Propaga exceções específicas de regras de negócio
        logger.warn("❌ Validação falhou: {}", e.getMessage());
        throw e;
    } catch (Exception e) {
        logger.error("❌ Erro ao atualizar status de pagamento: {}", e.getMessage(), e);
        throw new RuntimeException("Erro ao atualizar status de pagamento: " + e.getMessage());
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
