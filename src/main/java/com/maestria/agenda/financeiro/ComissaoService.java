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
    
    // Percentual padrão de comissão, configurável via application.properties
    @Value("${comissao.percentual}")
    private double comissaoPercentualPadrao;

    public ComissaoService(AgendamentoRepository agendamentoRepository,
            AgendamentoFixoRepository agendamentoFixoRepository,
            ProfissionalRepository profissionalRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.agendamentoFixoRepository = agendamentoFixoRepository;
        this.profissionalRepository = profissionalRepository;
    }

    /**
     * Lista todas as comissões de todos os profissionais dentro de um período específico
     * 
     * @param inicio Data inicial do período
     * @param fim Data final do período
     * @return Lista de DTOs com as comissões calculadas
     */
    public List<ComissaoResponseDTO> listarTodasComissoesNoPeriodo(LocalDate inicio, LocalDate fim) {
        try {
            // Buscar todos os profissionais
            List<Profissional> profissionais = profissionalRepository.findAll();
            List<ComissaoResponseDTO> comissoes = new ArrayList<>();

            // Iterar sobre cada profissional e calcular as comissões
            for (Profissional profissional : profissionais) {
                try {
                    // Calcular as comissões para o profissional no período
                    ComissaoResponseDTO comissao = calcularComissaoPorPeriodo(profissional.getId(), inicio, fim);
                    
                    // Adicionar à lista de comissões
                    comissoes.add(comissao);
                    
                } catch (Exception e) {
                    logger.error("❌ Erro ao calcular comissão para profissional {}: {}", 
                        profissional.getId(), e.getMessage());
                    // Continua para o próximo profissional
                }
            }

            return comissoes;
        } catch (Exception e) {
            logger.error("❌ Erro ao listar comissões no período", e);
            throw new RuntimeException("Erro ao listar comissões no período: " + e.getMessage());
        }
    }

    /**
     * Lista todas as comissões de todos os profissionais para o mês atual
     * @return Lista de DTOs com as comissões calculadas
     */
    public List<ComissaoResponseDTO> listarTodasComissoes() {
        try {
            // Buscar todos os profissionais
            List<Profissional> profissionais = profissionalRepository.findAll();
            List<ComissaoResponseDTO> comissoes = new ArrayList<>();
    
            // Definir um período padrão (exemplo: mês atual)
            LocalDate inicio = LocalDate.now().withDayOfMonth(1); // Primeiro dia do mês
            LocalDate fim = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()); // Último dia do mês
    
            // Iterar sobre cada profissional e calcular as comissões
            for (Profissional profissional : profissionais) {
                try {
                    // Calcular as comissões para o profissional no período
                    ComissaoResponseDTO comissao = calcularComissaoPorPeriodo(profissional.getId(), inicio, fim);
                    
                    // Adicionar à lista de comissões
                    comissoes.add(comissao);
                    
                } catch (Exception e) {
                    logger.error("❌ Erro ao calcular comissão para profissional {}: {}", 
                        profissional.getId(), e.getMessage());
                    // Continua para o próximo profissional
                }
            }
    
            return comissoes;
        } catch (Exception e) {
            logger.error("❌ Erro ao listar comissões", e);
            throw new RuntimeException("Erro ao listar comissões: " + e.getMessage());
        }
    }

    /**
     * Método legado para manter compatibilidade
     * @deprecated Use listarTodasComissoes() ou listarTodasComissoesNoPeriodo() em vez disso
     */
    @Deprecated
    public List<ComissaoResponseDTO> listarComissoes() {
        try {
            // Buscar todos os profissionais
            List<Profissional> profissionais = profissionalRepository.findAll();
    
            List<ComissaoResponseDTO> comissoes = new ArrayList<>();
    
            // Iterar sobre cada profissional e calcular as comissões
            for (Profissional profissional : profissionais) {
                // Definir um período padrão (exemplo: mês atual)
                LocalDate inicio = LocalDate.now().withDayOfMonth(1); // Primeiro dia do mês
                LocalDate fim = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()); // Último dia do mês
    
                try {
                    // Calcular as comissões para o profissional no período
                    ComissaoResponseDTO comissao = calcularComissaoPorPeriodo(profissional.getId(), inicio, fim);
        
                    // Adicionar à lista de comissões
                    comissoes.add(comissao);
                } catch (Exception e) {
                    logger.error("❌ Erro ao calcular comissão para profissional {}: {}", 
                        profissional.getId(), e.getMessage());
                    // Continua para o próximo profissional
                }
            }
    
            return comissoes;
        } catch (Exception e) {
            logger.error("❌ Erro ao listar comissões", e);
            throw new RuntimeException("Erro ao listar comissões: " + e.getMessage());
        }
    }

    /**
     * Calcula a comissão para um profissional específico em um período determinado
     * @param profissionalId ID do profissional
     * @param inicio Data inicial do período
     * @param fim Data final do período
     * @return DTO com os valores da comissão calculada
     */
    public ComissaoResponseDTO calcularComissaoPorPeriodo(Long profissionalId, LocalDate inicio, LocalDate fim) {
        try {
            Profissional profissional = profissionalRepository.findById(profissionalId)
                    .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

            // Buscar todos os agendamentos no período
            List<Agendamento> agendamentos = agendamentoRepository.findByProfissionalIdAndDataBetween(profissionalId,
                    inicio, fim);
            List<AgendamentoFixo> agendamentosFixos = agendamentoFixoRepository.findByProfissional(profissional);

            double comissaoTotal = 0.0;
            double comissaoLiquida = 0.0;
            double descontoTaxaTotal = 0.0;
            double comissaoAgendamentosNormais = 0.0;
            double comissaoAgendamentosFixos = 0.0;

            // Processar agendamentos normais
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

                PagamentoTipo pagamentoTipo = agendamento.getFormaPagamento();
                double taxaDesconto = (pagamentoTipo != null) ? pagamentoTipo.getTaxa() / 100 : 0.0;

                // Usar o percentual de comissão padrão em vez do campo do profissional
                double comissaoBruta = valorServico * (comissaoPercentualPadrao / 100);
                double comissaoComDesconto = comissaoBruta * (1 - taxaDesconto);

                comissaoTotal += comissaoBruta;
                comissaoLiquida += comissaoComDesconto;
                descontoTaxaTotal += comissaoBruta - comissaoComDesconto;
                comissaoAgendamentosNormais += comissaoBruta;
            }

            // Processar agendamentos fixos
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

                int diasExecutados = calcularDiasExecutadosNoPeriodo(agendamentoFixo, inicio, fim);
                PagamentoTipo pagamentoTipo = PagamentoTipo.fromString(agendamentoFixo.getFormaPagamento());
                double taxaDesconto = (pagamentoTipo != null) ? pagamentoTipo.getTaxa() / 100 : 0.0;

                // Usar o percentual de comissão padrão em vez do campo do profissional
                double comissaoBruta = valorServico * (comissaoPercentualPadrao / 100) * diasExecutados;
                double comissaoComDesconto = comissaoBruta * (1 - taxaDesconto);

                comissaoTotal += comissaoBruta;
                comissaoLiquida += comissaoComDesconto;
                descontoTaxaTotal += comissaoBruta - comissaoComDesconto;
                comissaoAgendamentosFixos += comissaoBruta;
            }

            // Retornar o DTO com os valores corretos
            return new ComissaoResponseDTO(
                    profissional.getId(),
                    profissional.getNome(),
                    inicio,
                    fim,
                    comissaoTotal,
                    comissaoLiquida,
                    comissaoAgendamentosNormais,
                    comissaoAgendamentosFixos,
                    descontoTaxaTotal);
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
        
        // Ajustar datas conforme necessário
        LocalDate dataInicio = agendamento.getDataInicio().isBefore(inicio) ? inicio : agendamento.getDataInicio();
        LocalDate dataFim = agendamento.getDataFim() == null || agendamento.getDataFim().isAfter(fim) ? 
                            fim : agendamento.getDataFim();
        
        LocalDate dataAtual = dataInicio;
        while (!dataAtual.isAfter(dataFim)) {
            boolean gerarOcorrencia = false;
            
            switch (agendamento.getTipoRepeticao()) {
                case DIARIA:
                    long diasDesdeInicio = dataAtual.toEpochDay() - agendamento.getDataInicio().toEpochDay();
                    gerarOcorrencia = diasDesdeInicio % agendamento.getIntervaloRepeticao() == 0;
                    break;
                case SEMANAL:
                    int diaDaSemana = dataAtual.getDayOfWeek().getValue() % 7 + 1; // 1=domingo
                    gerarOcorrencia = (agendamento.getValorRepeticao() & (1 << (diaDaSemana - 1))) != 0;
                    break;
                case MENSAL:
                    if (agendamento.getValorRepeticao() == -1) {
                        gerarOcorrencia = dataAtual.getDayOfMonth() == dataAtual.lengthOfMonth();
                    } else {
                        gerarOcorrencia = agendamento.getDiaDoMes() == dataAtual.getDayOfMonth();
                    }
                    break;
                case QUINZENAL:
                    long diasDesdeInicioQuinzenal = dataAtual.toEpochDay() - agendamento.getDataInicio().toEpochDay();
                    gerarOcorrencia = diasDesdeInicioQuinzenal % 15 == 0;
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
}