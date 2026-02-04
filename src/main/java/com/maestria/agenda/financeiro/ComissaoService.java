package com.maestria.agenda.financeiro;

import com.maestria.agenda.agendamento.Agendamento;
import com.maestria.agenda.agendamento.AgendamentoFixo;
import com.maestria.agenda.agendamento.AgendamentoFixoRepository;
import com.maestria.agenda.agendamento.AgendamentoRepository;
import com.maestria.agenda.profissional.Profissional;
import com.maestria.agenda.profissional.ProfissionalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class ComissaoService {

        private final AgendamentoRepository agendamentoRepository;
        private final AgendamentoFixoRepository agendamentoFixoRepository;
        private final ProfissionalRepository profissionalRepository;
        private final ComissaoPagamentoRepository comissaoPagamentoRepository;
        private final TaxaPagamentoService taxaPagamentoService;
        private final Logger logger = LoggerFactory.getLogger(ComissaoService.class);

        // Removida a injeção da comissão global pois agora cada serviço tem sua própria
        // comissão

        public ComissaoService(AgendamentoRepository agendamentoRepository,
                        AgendamentoFixoRepository agendamentoFixoRepository,
                        ProfissionalRepository profissionalRepository,
                        ComissaoPagamentoRepository comissaoPagamentoRepository,
                        TaxaPagamentoService taxaPagamentoService) {
                this.agendamentoRepository = agendamentoRepository;
                this.agendamentoFixoRepository = agendamentoFixoRepository;
                this.profissionalRepository = profissionalRepository;
                this.comissaoPagamentoRepository = comissaoPagamentoRepository;
                this.taxaPagamentoService = taxaPagamentoService;
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
        private ResultadoComissao calcularComissaoAgendamentosNormais(Long profissionalId, LocalDate inicio,
                        LocalDate fim) {
                logger.info("Calculando comissão de agendamentos normais para profissional {} entre {} e {}",
                                profissionalId, inicio, fim);

                double valorTotal = 0.0;
                double comissaoTotal = 0.0;
                double descontoTaxaTotal = 0.0;

                // OTIMIZAÇÃO: Usa query com FETCH JOIN para carregar cliente e servico em uma
                // única query
                // Evita problema N+1 (antes: 1 query + N queries para cliente + N queries para
                // servico)
                List<Agendamento> agendamentosNormais = agendamentoRepository
                                .findAgendamentosNormaisComDetalhes(profissionalId, inicio, fim);

                logger.info("Encontrados {} agendamentos normais (não fixos)", agendamentosNormais.size());

                for (Agendamento agendamento : agendamentosNormais) {
                        if (agendamento.getPago() != null && agendamento.getPago() &&
                                        agendamento.getServico() != null
                                        && agendamento.getServico().getValor() != null) {

                                double valorServico = agendamento.getServico().getValor();
                                // Se não há comissão definida, usa 70% como padrão (valor anterior)
                                double comissaoPercentualServico = agendamento.getServico()
                                                .getComissaoPercentual() != null
                                                                ? agendamento.getServico().getComissaoPercentual()
                                                                : 70.0;
                                double taxa = 0.0;

                                if (agendamento.getFormaPagamento() != null) {
                                        taxa = taxaPagamentoService.obterTaxa(agendamento.getFormaPagamento());
                                }

                                double descontoTaxa = valorServico * (taxa / 100.0);
                                double comissaoServico = valorServico * (comissaoPercentualServico / 100.0);

                                valorTotal += valorServico;
                                comissaoTotal += comissaoServico;
                                descontoTaxaTotal += descontoTaxa;

                                logger.debug(
                                                "Agendamento normal ID {}: valor {}, comissão {}%, valor comissão {}, taxa {}%, desconto {}",
                                                agendamento.getId(), valorServico, comissaoPercentualServico,
                                                comissaoServico, taxa,
                                                descontoTaxa);
                        }
                }

                logger.info("Comissão de agendamentos normais: valor total {}, comissão total {}, desconto {}",
                                valorTotal, comissaoTotal, descontoTaxaTotal);

                return new ResultadoComissao(valorTotal, comissaoTotal, descontoTaxaTotal);
        }

        /**
         * Calcula a comissão para agendamentos fixos
         */
        private ResultadoComissao calcularComissaoAgendamentosFixos(Long profissionalId, LocalDate inicio,
                        LocalDate fim) {
                logger.info("Calculando comissão de agendamentos fixos para profissional {} entre {} e {}",
                                profissionalId, inicio, fim);

                double valorTotal = 0.0;
                double comissaoTotal = 0.0;
                double descontoTaxaTotal = 0.0;

                List<AgendamentoFixo> agendamentosFixos = agendamentoFixoRepository
                                .findByProfissionalIdAndAtivoTrue(profissionalId);

                logger.info("Encontrados {} agendamentos fixos ativos", agendamentosFixos.size());

                for (AgendamentoFixo agendamentoFixo : agendamentosFixos) {
                        List<Agendamento> agendamentosGerados = agendamentoRepository
                                        .findByAgendamentoFixoIdAndDataBetweenAndPagoTrue(agendamentoFixo.getId(),
                                                        inicio, fim);

                        if (!agendamentosGerados.isEmpty() &&
                                        agendamentoFixo.getServico() != null &&
                                        agendamentoFixo.getServico().getValor() != null) {

                                double valorServico = agendamentoFixo.getServico().getValor();
                                // Se não há comissão definida, usa 70% como padrão (valor anterior)
                                double comissaoPercentualServico = agendamentoFixo.getServico()
                                                .getComissaoPercentual() != null
                                                                ? agendamentoFixo.getServico().getComissaoPercentual()
                                                                : 70.0;
                                double valorTotalServico = valorServico * agendamentosGerados.size();
                                double comissaoTotalServico = valorTotalServico * (comissaoPercentualServico / 100.0);
                                double descontoTaxa = 0.0;

                                for (Agendamento agendamento : agendamentosGerados) {
                                        if (agendamento.getFormaPagamento() != null) {
                                                double taxa = taxaPagamentoService
                                                                .obterTaxa(agendamento.getFormaPagamento());
                                                descontoTaxa += valorServico * (taxa / 100.0);
                                        }
                                }

                                valorTotal += valorTotalServico;
                                comissaoTotal += comissaoTotalServico;
                                descontoTaxaTotal += descontoTaxa;

                                logger.debug(
                                                "Agendamento fixo ID {}: {} ocorrências pagas x {} = {}, comissão {}%, valor comissão {}, desconto {}",
                                                agendamentoFixo.getId(), agendamentosGerados.size(), valorServico,
                                                valorTotalServico, comissaoPercentualServico, comissaoTotalServico,
                                                descontoTaxa);
                        }
                }

                logger.info("Comissão de agendamentos fixos: valor total {}, comissão total {}, desconto {}",
                                valorTotal, comissaoTotal, descontoTaxaTotal);

                return new ResultadoComissao(valorTotal, comissaoTotal, descontoTaxaTotal);
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
                        ResultadoComissao resultadoNormal = calcularComissaoAgendamentosNormais(profissionalId, inicio,
                                        fim);

                        // Calcular comissão de agendamentos fixos
                        ResultadoComissao resultadoFixo = calcularComissaoAgendamentosFixos(profissionalId, inicio,
                                        fim);

                        // Somar os resultados
                        double comissaoTotal = resultadoNormal.valorComissao + resultadoFixo.valorComissao;
                        double descontoTaxaTotal = resultadoNormal.valorDescontoTaxa + resultadoFixo.valorDescontoTaxa;

                        // Calcular comissão líquida baseada na configuração do profissional
                        double comissaoLiquida;
                        if (profissional.getDescontarTaxas() != null && profissional.getDescontarTaxas()) {
                                // Profissional tem desconto de taxas
                                comissaoLiquida = comissaoTotal - descontoTaxaTotal;
                                logger.info("Profissional {} TEM desconto de taxas: {} - {} = {}",
                                                profissional.getNome(), comissaoTotal, descontoTaxaTotal,
                                                comissaoLiquida);
                        } else {
                                // Profissional NÃO tem desconto de taxas
                                comissaoLiquida = comissaoTotal;
                                logger.info("Profissional {} NÃO TEM desconto de taxas: {} (taxas ignoradas: {})",
                                                profissional.getNome(), comissaoLiquida, descontoTaxaTotal);
                        }

                        // Calcular valor já pago no período
                        double valorJaPago = comissaoPagamentoRepository.calcularValorTotalPagoNoPeriodo(profissionalId,
                                        inicio,
                                        fim);

                        // Buscar todas as comissões do período
                        List<ComissaoPagamento> comissoes = comissaoPagamentoRepository
                                        .findByProfissionalIdAndPeriodo(profissionalId, inicio, fim);
                        List<ComissaoIndividualDTO> comissoesIndividuais = comissoes.stream()
                                        .map(comissao -> new ComissaoIndividualDTO(
                                                        comissao.getId(),
                                                        comissao.getAgendamentoId(),
                                                        comissao.getDataPagamento(),
                                                        comissao.getDataHoraPagamento(),
                                                        comissao.getValorComissao(),
                                                        comissao.getStatus().toString(),
                                                        comissao.getPaid()))
                                        .collect(Collectors.toList());

                        logger.info("Comissão de agendamentos normais: {} bruto, {} líquido, {} desconto",
                                        resultadoNormal.valorComissao, resultadoNormal.valorComissaoLiquida,
                                        resultadoNormal.valorDescontoTaxa);
                        logger.info("Comissão de agendamentos fixos: {} bruto, {} líquido, {} desconto",
                                        resultadoFixo.valorComissao, resultadoFixo.valorComissaoLiquida,
                                        resultadoFixo.valorDescontoTaxa);
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
         * Suporta múltiplos pagamentos parciais para o mesmo período
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

                // Calcular a comissão do período
                ComissaoResponseDTO comissao = calcularComissaoPorPeriodo(profissionalId, periodoInicio, periodoFim);

                // Buscar pagamentos existentes (não cancelados) para este período
                List<ComissaoPagamento> pagamentosExistentes = comissaoPagamentoRepository
                                .findByProfissionalIdAndPeriodo(profissionalId, periodoInicio, periodoFim);

                // Calcular total já pago (apenas pagamentos não cancelados)
                double totalJaPago = pagamentosExistentes.stream()
                                .filter(p -> p.getStatus() != ComissaoPagamento.StatusPagamento.CANCELADO)
                                .mapToDouble(ComissaoPagamento::getValorPago)
                                .sum();

                logger.info(
                                "📊 Validação de pagamento: Comissão Líquida={}, Total Já Pago={}, Novo Pagamento={}, Total Final={}",
                                comissao.getComissaoLiquida(), totalJaPago, valorPago, totalJaPago + valorPago);

                // Verificar se o novo pagamento + pagamentos existentes não excedem a comissão
                // líquida
                double totalComNovoPagamento = totalJaPago + valorPago;
                if (totalComNovoPagamento > comissao.getComissaoLiquida()) {
                        double valorMaximoPermitido = comissao.getComissaoLiquida() - totalJaPago;
                        throw new RuntimeException(
                                        String.format("Valor do pagamento (R$ %.2f) excede o valor pendente (R$ %.2f). "
                                                        +
                                                        "Total já pago: R$ %.2f. Comissão líquida: R$ %.2f. " +
                                                        "Valor máximo permitido para este pagamento: R$ %.2f",
                                                        valorPago, comissao.getValorPendente(), totalJaPago,
                                                        comissao.getComissaoLiquida(), valorMaximoPermitido));
                }

                // Verificar se o valor pago é válido (usando valorPendente que já considera
                // pagamentos anteriores)
                if (valorPago > comissao.getValorPendente()) {
                        throw new RuntimeException(
                                        String.format("Valor pago (R$ %.2f) não pode ser maior que o valor pendente (R$ %.2f)",
                                                        valorPago, comissao.getValorPendente()));
                }

                // Criar o registro de pagamento
                ComissaoPagamento pagamento = new ComissaoPagamento(
                                profissionalId,
                                null, // agendamentoId será null para pagamentos gerais
                                dataPagamento,
                                valorPago,
                                observacao,
                                periodoInicio,
                                periodoFim);

                // Definir a data e hora exata do pagamento no fuso horário de São Paulo
                ZoneId zonaSaoPaulo = ZoneId.of("America/Sao_Paulo");
                LocalDateTime dataHoraSaoPaulo = LocalDateTime.now(zonaSaoPaulo);
                pagamento.setDataHoraPagamento(dataHoraSaoPaulo);

                // Definir o valor da comissão como o valor pago e garantir que paid seja true
                pagamento.setValorComissao(valorPago);
                pagamento.setPaid(true);

                // Salvar o pagamento
                ComissaoPagamento pagamentoSalvo = comissaoPagamentoRepository.save(pagamento);

                logger.info("✅ Pagamento registrado com sucesso! ID={}, Valor={}, Profissional={}",
                                pagamentoSalvo.getId(), valorPago, profissionalId);

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
        public List<ComissaoPagamento> listarPagamentosPorPeriodo(Long profissionalId, LocalDate inicio,
                        LocalDate fim) {
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
                                pagamento.getPeriodoFim());
        }

        /**
         * Limpa pagamentos inválidos (zerados) de um profissional em um período
         */
        public List<ComissaoResponseDTO> limparPagamentosInvalidos(Long profissionalId, LocalDate inicio,
                        LocalDate fim) {
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
                        if (valorRestante <= 0)
                                break;

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

        /**
         * Cancela parcialmente um pagamento de comissão
         */
        public ComissaoResponseDTO cancelarParcialmentePagamentoComissao(Long pagamentoId, Double valorACancelar) {
                logger.info("❌ Cancelando parcialmente pagamento de comissão ID: {} no valor de {}",
                                pagamentoId, valorACancelar);

                ComissaoPagamento pagamento = comissaoPagamentoRepository.findById(pagamentoId)
                                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));

                if (pagamento.getStatus() == ComissaoPagamento.StatusPagamento.CANCELADO) {
                        throw new RuntimeException("Este pagamento já está cancelado");
                }

                // Cancelar parcialmente o pagamento
                pagamento.cancelarParcialmente(valorACancelar);
                comissaoPagamentoRepository.save(pagamento);

                // Recalcular a comissão para retornar os valores atualizados
                return calcularComissaoPorPeriodo(
                                pagamento.getProfissionalId(),
                                pagamento.getPeriodoInicio(),
                                pagamento.getPeriodoFim());
        }
}
