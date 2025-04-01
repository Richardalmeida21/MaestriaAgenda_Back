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

    // Calcular comissão total por período para um profissional
    public ComissaoResponseDTO calcularComissaoPorPeriodo(Long profissionalId, LocalDate inicio, LocalDate fim) {
        logger.info("🔍 Calculando comissão para o profissional {} entre {} e {}",
                profissionalId, inicio, fim);

        try {
            // Buscar o profissional
            Profissional profissional = profissionalRepository.findById(profissionalId)
                    .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

            // Calcular comissão dos agendamentos normais
            Double comissaoAgendamentos = agendamentoRepository.calcularComissaoTotalPorPeriodo(
                    profissionalId, inicio, fim, comissaoPercentual / 100);

            if (comissaoAgendamentos == null)
                comissaoAgendamentos = 0.0;

            // Calcular comissão dos agendamentos fixos
            Double comissaoAgendamentosFixos = calcularComissaoAgendamentosFixos(profissional, inicio, fim);

            // Total
            Double comissaoTotal = comissaoAgendamentos + comissaoAgendamentosFixos;

            logger.info("✅ Comissão calculada: R$ {}", comissaoTotal);

            // Usar a classe em vez do record
            return new ComissaoResponseDTO(
                    profissional.getId(),
                    profissional.getNome(),
                    inicio,
                    fim,
                    comissaoTotal,
                    comissaoAgendamentos,
                    comissaoAgendamentosFixos);
        } catch (Exception e) {
            logger.error("❌ Erro ao calcular comissão", e);
            throw new RuntimeException("Erro ao calcular comissão: " + e.getMessage());
        }
    }

    // Listar comissões para todos os profissionais
    public List<Object[]> listarComissoes() {
        return agendamentoRepository.calcularComissaoPorProfissional(comissaoPercentual / 100);
    }

    // Método auxiliar para calcular comissões de agendamentos fixos
    private Double calcularComissaoAgendamentosFixos(Profissional profissional, LocalDate inicio, LocalDate fim) {
        logger.info("🔍 Calculando comissão de agendamentos fixos para o profissional {} entre {} e {}",
                profissional.getId(), inicio, fim);

        try {
            // Buscar agendamentos fixos ativos no período
            List<AgendamentoFixo> agendamentosFixos = agendamentoFixoRepository.findByProfissional(profissional)
                    .stream()
                    .filter(af -> af.getDataInicio().compareTo(fim) <= 0 &&
                            (af.getDataFim() == null || af.getDataFim().compareTo(inicio) >= 0))
                    .toList();

            // Calcular comissão
            Double comissaoTotal = 0.0;
            for (AgendamentoFixo agendamentoFixo : agendamentosFixos) {
                // Verificar se o agendamento tem um serviço válido
                if (agendamentoFixo.getServico() == null) {
                    logger.warn("⚠️ Agendamento fixo ID {} não possui serviço associado", agendamentoFixo.getId());
                    continue;
                }

                // Considerar apenas os dias que o agendamento fixo seria executado no período
                int diasExecutados = calcularDiasExecutadosNoPeriodo(agendamentoFixo, inicio, fim);

                // Obter valor do serviço
                Double valorServico = agendamentoFixo.getServico().getValor();
                if (valorServico == null) {
                    logger.warn("⚠️ Serviço do agendamento fixo ID {} não tem valor definido", agendamentoFixo.getId());
                    continue;
                }

                // Calcular comissão para este agendamento fixo
                Double valorComissao = valorServico * (comissaoPercentual / 100) * diasExecutados;
                comissaoTotal += valorComissao;
            }

            logger.info("✅ Comissão de agendamentos fixos calculada: R$ {}", comissaoTotal);
            return comissaoTotal;
        } catch (Exception e) {
            logger.error("❌ Erro ao calcular comissão de agendamentos fixos", e);
            return 0.0; // Em caso de erro, retorna zero
        }
    }

    // Método auxiliar para calcular quantas vezes um agendamento fixo seria
    // executado em um período
    private int calcularDiasExecutadosNoPeriodo(AgendamentoFixo agendamento, LocalDate inicio, LocalDate fim) {
        int diasExecutados = 0;

        // Ajustar início e fim para não ultrapassar os limites do agendamento fixo
        LocalDate inicioEfetivo = inicio.isBefore(agendamento.getDataInicio()) ? agendamento.getDataInicio() : inicio;

        LocalDate fimEfetivo = (agendamento.getDataFim() != null && fim.isAfter(agendamento.getDataFim()))
                ? agendamento.getDataFim()
                : fim;

        // Se o período não é válido, retorna 0
        if (inicioEfetivo.isAfter(fimEfetivo)) {
            return 0;
        }

        LocalDate atual;

        // Calcular baseado no tipo de repetição
        switch (agendamento.getTipoRepeticao()) {
            case DIARIA:
                // Para repetição diária, calcular quantos dias no intervalo são execuções
                // válidas
                long totalDias = fimEfetivo.toEpochDay() - inicioEfetivo.toEpochDay() + 1;
                diasExecutados = (int) (totalDias / agendamento.getIntervaloRepeticao());
                break;

            case SEMANAL:
                // Para repetição semanal, contar semanas e verificar o dia da semana
                atual = inicioEfetivo;
                while (!atual.isAfter(fimEfetivo)) {
                    // Verificar se o dia da semana corresponde ao valorRepeticao
                    if (atual.getDayOfWeek().getValue() == agendamento.getValorRepeticao()) {
                        // Verificar se a semana está no intervalo correto
                        long semanasDesdoInicio = (atual.toEpochDay() - agendamento.getDataInicio().toEpochDay()) / 7;
                        if (semanasDesdoInicio % agendamento.getIntervaloRepeticao() == 0) {
                            diasExecutados++;
                        }
                    }
                    atual = atual.plusDays(1);
                }
                break;

            case MENSAL:
                // Para repetição mensal, verificar cada mês no período
                atual = inicioEfetivo;
                while (!atual.isAfter(fimEfetivo)) {
                    boolean executa = false;

                    // Verificar se é o último dia do mês quando valorRepeticao é -1
                    if (agendamento.getValorRepeticao() == -1) {
                        executa = atual.getDayOfMonth() == atual.getMonth().length(atual.isLeapYear());
                    }
                    // Verificar se é o dia específico do mês
                    else if (atual.getDayOfMonth() == agendamento.getValorRepeticao()) {
                        executa = true;
                    }

                    if (executa) {
                        // Verificar se o mês está no intervalo correto
                        int mesesDesdoInicio = (atual.getYear() - agendamento.getDataInicio().getYear()) * 12 +
                                atual.getMonthValue() - agendamento.getDataInicio().getMonthValue();
                        if (mesesDesdoInicio % agendamento.getIntervaloRepeticao() == 0) {
                            diasExecutados++;
                        }
                    }

                    atual = atual.plusDays(1);
                }
                break;
        }

        return diasExecutados;
    }

}