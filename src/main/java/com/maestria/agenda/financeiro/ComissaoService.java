package com.maestria.agenda.financeiro;

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
import java.util.List;

@Service
public class ComissaoService {
    private static final Logger logger = LoggerFactory.getLogger(ComissaoService.class);

    private final AgendamentoRepository agendamentoRepository;
    private final AgendamentoFixoRepository agendamentoFixoRepository;
    private final ProfissionalRepository profissionalRepository;

    // Percentual de comissão que incide sobre o valor (ex: 20% = 20.0)
    @Value("${comissao.percentual}")
    private double comissaoPercentual;

    public ComissaoService(
            AgendamentoRepository agendamentoRepository,
            AgendamentoFixoRepository agendamentoFixoRepository,
            ProfissionalRepository profissionalRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.agendamentoFixoRepository = agendamentoFixoRepository;
        this.profissionalRepository = profissionalRepository;
    }

    // Calcula a comissão total por período para um profissional
    public ComissaoResponseDTO calcularComissaoPorPeriodo(Long profissionalId, LocalDate inicio, LocalDate fim) {
    try {
        Profissional profissional = profissionalRepository.findById(profissionalId)
            .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

        // Buscar todos os agendamentos no período
        List<Agendamento> agendamentos = agendamentoRepository.findByProfissionalIdAndDataBetween(profissionalId, inicio, fim);
        List<AgendamentoFixo> agendamentosFixos = agendamentoFixoRepository.findByProfissional(profissional);

        double comissaoTotal = 0.0;
        double comissaoLiquida = 0.0;

        // 🔹 Processar agendamentos normais
        for (Agendamento agendamento : agendamentos) {
            if (agendamento.getServico() == null) {
                logger.warn("⚠️ Agendamento ID {} não possui serviço associado", agendamento.getId());
                continue;
            }

            Double valorServico = agendamento.getServico().getValor();
            if (valorServico == null) {
                logger.warn("⚠️ Serviço do agendamento ID {} não tem valor definido", agendamento.getId());
                continue;
            }

            // 🔹 Identificar a forma de pagamento e sua taxa
            PagamentoTipo pagamentoTipo = PagamentoTipo.fromString(agendamento.getFormaPagamento());
            double taxaDesconto = (pagamentoTipo != null) ? pagamentoTipo.getTaxa() / 100 : 0.0;

            // 🔹 Calcular comissão bruta e líquida para este agendamento
            double comissaoBruta = valorServico * (comissaoPercentual / 100);
            double comissaoComDesconto = comissaoBruta * (1 - taxaDesconto);

            // 🔹 Acumular valores
            comissaoTotal += comissaoBruta;
            comissaoLiquida += comissaoComDesconto;
        }

        // 🔹 Processar agendamentos fixos
        for (AgendamentoFixo agendamentoFixo : agendamentosFixos) {
            if (agendamentoFixo.getServico() == null) {
                logger.warn("⚠️ Agendamento fixo ID {} não possui serviço associado", agendamentoFixo.getId());
                continue;
            }

            Double valorServico = agendamentoFixo.getServico().getValor();
            if (valorServico == null) {
                logger.warn("⚠️ Serviço do agendamento fixo ID {} não tem valor definido", agendamentoFixo.getId());
                continue;
            }

            // 🔹 Identificar a forma de pagamento e sua taxa
            PagamentoTipo pagamentoTipo = PagamentoTipo.fromString(agendamentoFixo.getFormaPagamento());
            double taxaDesconto = (pagamentoTipo != null) ? pagamentoTipo.getTaxa() / 100 : 0.0;

            // 🔹 Contar quantas vezes o agendamento fixo foi realizado no período
            int diasExecutados = calcularDiasExecutadosNoPeriodo(agendamentoFixo, inicio, fim);

            // 🔹 Calcular comissão bruta e líquida
            double comissaoBruta = valorServico * (comissaoPercentual / 100) * diasExecutados;
            double comissaoComDesconto = comissaoBruta * (1 - taxaDesconto);

            // 🔹 Acumular valores
            comissaoTotal += comissaoBruta;
            comissaoLiquida += comissaoComDesconto;
        }

        // 🔹 Retornar o DTO com os valores corretos
        return new ComissaoResponseDTO(
            profissional.getId(),
            profissional.getNome(),
            inicio,
            fim,
            comissaoTotal,
            comissaoLiquida,  
            comissaoTotal - comissaoLiquida,  
            comissaoLiquida
        );
    } catch (Exception e) {
        logger.error("❌ Erro ao calcular comissão", e);
        throw new RuntimeException("Erro ao calcular comissão: " + e.getMessage());
    }
}


    // Lista comissões para todos os profissionais (consulta customizada, se necessário)
    public List<Object[]> listarComissoes() {
        return agendamentoRepository.calcularComissaoPorProfissional(comissaoPercentual / 100);
    }

    // Método auxiliar para calcular comissão dos agendamentos fixos
    private Double calcularComissaoAgendamentosFixos(Profissional profissional, LocalDate inicio, LocalDate fim) {
        logger.info("🔍 Calculando comissão de agendamentos fixos para o profissional {} entre {} e {}",
                profissional.getId(), inicio, fim);
        try {
            // Busca agendamentos fixos ativos no período
            List<AgendamentoFixo> agendamentosFixos = agendamentoFixoRepository.findByProfissional(profissional)
                    .stream()
                    .filter(af -> af.getDataInicio().compareTo(fim) <= 0 &&
                            (af.getDataFim() == null || af.getDataFim().compareTo(inicio) >= 0))
                    .toList();

            Double comissaoTotal = 0.0;
            for (AgendamentoFixo af : agendamentosFixos) {
                // Se não houver serviço associado, ignorar
                if (af.getServico() == null) {
                    logger.warn("⚠️ Agendamento fixo ID {} não possui serviço associado", af.getId());
                    continue;
                }

                // Quantas vezes o agendamento fixo foi executado no período
                int diasExecutados = calcularDiasExecutadosNoPeriodo(af, inicio, fim);

                // Obter valor do serviço
                Double valorServico = af.getServico().getValor();
                if (valorServico == null) {
                    logger.warn("⚠️ Serviço do agendamento fixo ID {} não tem valor definido", af.getId());
                    continue;
                }

                // Converter a forma de pagamento para o Enum para obter a taxa
                PagamentoTipo pagamento = PagamentoTipo.fromString(af.getFormaPagamento());
                double taxaAplicada = (pagamento != null) ? pagamento.getTaxa() : 0.0;
                // Calcula o valor líquido, descontando a taxa
                double valorLiquido = valorServico * (1 - taxaAplicada / 100);

                // Comissão para este agendamento fixo
                Double valorComissao = valorLiquido * (comissaoPercentual / 100) * diasExecutados;
                comissaoTotal += valorComissao;
            }
            logger.info("✅ Comissão de agendamentos fixos calculada: R$ {}", comissaoTotal);
            return comissaoTotal;
        } catch (Exception e) {
            logger.error("❌ Erro ao calcular comissão de agendamentos fixos", e);
            return 0.0;
        }
    }

    // Método auxiliar para calcular quantas vezes um agendamento fixo seria executado no período
    private int calcularDiasExecutadosNoPeriodo(AgendamentoFixo agendamento, LocalDate inicio, LocalDate fim) {
        int diasExecutados = 0;
        LocalDate inicioEfetivo = inicio.isBefore(agendamento.getDataInicio()) ? agendamento.getDataInicio() : inicio;
        LocalDate fimEfetivo = (agendamento.getDataFim() != null && fim.isAfter(agendamento.getDataFim()))
                ? agendamento.getDataFim()
                : fim;
        if (inicioEfetivo.isAfter(fimEfetivo)) {
            return 0;
        }

        LocalDate atual;
        switch (agendamento.getTipoRepeticao()) {
            case DIARIA:
                long totalDias = fimEfetivo.toEpochDay() - inicioEfetivo.toEpochDay() + 1;
                diasExecutados = (int) (totalDias / agendamento.getIntervaloRepeticao());
                break;
            case SEMANAL:
                atual = inicioEfetivo;
                while (!atual.isAfter(fimEfetivo)) {
                    if (atual.getDayOfWeek().getValue() == agendamento.getValorRepeticao()) {
                        long semanasDesdoInicio = (atual.toEpochDay() - agendamento.getDataInicio().toEpochDay()) / 7;
                        if (semanasDesdoInicio % agendamento.getIntervaloRepeticao() == 0) {
                            diasExecutados++;
                        }
                    }
                    atual = atual.plusDays(1);
                }
                break;
            case MENSAL:
                atual = inicioEfetivo;
                while (!atual.isAfter(fimEfetivo)) {
                    boolean executa = false;
                    if (agendamento.getValorRepeticao() == -1) {
                        executa = atual.getDayOfMonth() == atual.getMonth().length(atual.isLeapYear());
                    } else if (atual.getDayOfMonth() == agendamento.getValorRepeticao()) {
                        executa = true;
                    }
                    if (executa) {
                        int mesesDesdoInicio = (atual.getYear() - agendamento.getDataInicio().getYear()) * 12 +
                                atual.getMonthValue() - agendamento.getDataInicio().getMonthValue();
                        if (mesesDesdoInicio % agendamento.getIntervaloRepeticao() == 0) {
                            diasExecutados++;
                        }
                    }
                    atual = atual.plusDays(1);
                }
                break;
            default:
                break;
        }
        return diasExecutados;
    }
}
