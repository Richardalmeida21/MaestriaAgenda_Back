package com.maestria.agenda.controller;

import com.maestria.agenda.agendamento.Agendamento;
import com.maestria.agenda.agendamento.AgendamentoFixo;
import com.maestria.agenda.agendamento.AgendamentoFixoRepository;
import com.maestria.agenda.agendamento.AgendamentoRepository;
import com.maestria.agenda.agendamento.DadosCadastroAgendamento;
import com.maestria.agenda.agendamento.DadosCadastroAgendamentoFixo;
import com.maestria.agenda.bloqueio.BloqueioAgenda;
import com.maestria.agenda.bloqueio.BloqueioAgendaRepository;
import com.maestria.agenda.cliente.Cliente;
import com.maestria.agenda.cliente.ClienteRepository;
import com.maestria.agenda.profissional.Profissional;
import com.maestria.agenda.profissional.ProfissionalRepository;
import com.maestria.agenda.servico.Servico;
import com.maestria.agenda.servico.ServicoRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

@RestController
@RequestMapping("/agendamento")
public class AgendamentoController {

    private static final Logger logger = LoggerFactory.getLogger(AgendamentoController.class);

    private final AgendamentoRepository agendamentoRepository;
    private final AgendamentoFixoRepository agendamentoFixoRepository;
    private final ClienteRepository clienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final ServicoRepository servicoRepository;
    private final BloqueioAgendaRepository bloqueioRepository;

    @Value("${comissao.percentual}")
    private double comissaoPercentual;

    // Método auxiliar para calcular comissão de agendamentos fixos
    private Double calcularComissaoAgendamentosFixos(Long profissionalId, LocalDate inicio, LocalDate fim) {
        logger.info("🔍 Calculando comissão de agendamentos fixos para o profissional {} entre {} e {}",
                profissionalId, inicio, fim);

        try {
            // Buscar o profissional
            Profissional profissional = profissionalRepository.findById(profissionalId)
                    .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

            // Buscar agendamentos fixos ativos no período
            List<AgendamentoFixo> agendamentosFixos = agendamentoFixoRepository.findByProfissional(profissional)
                    .stream()
                    .filter(af -> af.getDataInicio().compareTo(fim) <= 0 &&
                            (af.getDataFim() == null || af.getDataFim().compareTo(inicio) >= 0))
                    .toList();

            // Calcular comissão
            Double comissaoTotal = 0.0;
            for (AgendamentoFixo agendamentoFixo : agendamentosFixos) {
                // Considerar apenas os dias que o agendamento fixo seria executado no período
                int diasExecutados = calcularDiasExecutadosNoPeriodo(agendamentoFixo, inicio, fim);

                // Calcular comissão para este agendamento fixo
                Double valorComissao = agendamentoFixo.getValor() * (comissaoPercentual / 100) * diasExecutados;
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
                LocalDate atual = inicioEfetivo;
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
                LocalDate atualMensal = inicioEfetivo;
                while (!atualMensal.isAfter(fimEfetivo)) {
                    boolean executa = false;

                    // Verificar se é o último dia do mês quando valorRepeticao é -1
                    if (agendamento.getValorRepeticao() == -1) {
                        executa = atualMensal.getDayOfMonth() == atualMensal.getMonth().length(atualMensal.isLeapYear());
                    }
                    // Verificar se é o dia específico do mês
                    else if (atualMensal.getDayOfMonth() == agendamento.getValorRepeticao()) {
                        executa = true;
                    }

                    if (executa) {
                        // Verificar se o mês está no intervalo correto
                        int mesesDesdoInicio = (atualMensal.getYear() - agendamento.getDataInicio().getYear()) * 12 +
                                atualMensal.getMonthValue() - agendamento.getDataInicio().getMonthValue();
                        if (mesesDesdoInicio % agendamento.getIntervaloRepeticao() == 0) {
                            diasExecutados++;
                        }
                    }

                    atualMensal = atualMensal.plusDays(1);
                }
                break;
        }

        return diasExecutados;
    }

    public AgendamentoController(AgendamentoRepository agendamentoRepository,
            AgendamentoFixoRepository agendamentoFixoRepository,
            ClienteRepository clienteRepository,
            ProfissionalRepository profissionalRepository,
            ServicoRepository servicoRepository,
            BloqueioAgendaRepository bloqueioRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.agendamentoFixoRepository = agendamentoFixoRepository;
        this.clienteRepository = clienteRepository;
        this.profissionalRepository = profissionalRepository;
        this.servicoRepository = servicoRepository;
        this.bloqueioRepository = bloqueioRepository;
    }

    // ✅ Endpoint para criar agendamentos fixos com o novo modelo de repetição
    @PostMapping("/fixo")
    public ResponseEntity<?> cadastrarAgendamentoFixo(
            @RequestBody DadosCadastroAgendamentoFixo dados,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔍 Solicitação para criar agendamento fixo por: {}", userDetails.getUsername());

        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            logger.warn("❌ Tentativa de criação de agendamento fixo sem permissão por {}", userDetails.getUsername());
            return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode criar agendamentos fixos.");
        }

        try {
            // Buscar o cliente, profissional e serviço
            Cliente cliente = clienteRepository.findById(dados.clienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

            Profissional profissional = profissionalRepository.findById(dados.profissionalId())
                    .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

            Servico servico = servicoRepository.findById(dados.servicoId())
                    .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

            // Criar um novo AgendamentoFixo com os dados recebidos
            AgendamentoFixo agendamentoFixo = new AgendamentoFixo();
            agendamentoFixo.setCliente(cliente);
            agendamentoFixo.setProfissional(profissional);
            agendamentoFixo.setServico(servico);
            agendamentoFixo.setTipoRepeticao(dados.tipoRepeticao());
            agendamentoFixo.setIntervaloRepeticao(dados.intervaloRepeticao());
            agendamentoFixo.setValorRepeticao(dados.valorRepeticao());
            agendamentoFixo.setDataInicio(dados.dataInicio());
            agendamentoFixo.setDataFim(dados.dataFim());
            agendamentoFixo.setHora(dados.hora());
            agendamentoFixo.setDuracao(dados.duracao());
            agendamentoFixo.setObservacao(dados.observacao());
            agendamentoFixo.setValor(dados.valor());

            agendamentoFixoRepository.save(agendamentoFixo);
            logger.info("✅ Agendamento fixo criado com sucesso: {}", agendamentoFixo);
            return ResponseEntity.ok(agendamentoFixo);
        } catch (Exception e) {
            logger.error("❌ Erro ao criar agendamento fixo", e);
            return ResponseEntity.status(500).body("Erro ao criar agendamento fixo: " + e.getMessage());
        }
    }

    // ✅ Endpoint para listar agendamentos fixos com suporte ao novo modelo de
    // repetição
    // ✅ Profissionais também podem ver seus agendamentos fixos, mas não
    // modificá-los
    @GetMapping("/fixo")
    public ResponseEntity<?> listarAgendamentosFixos(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔍 Solicitação para listar agendamentos fixos por: {}", userDetails.getUsername());

        try {
            List<AgendamentoFixo> agendamentosFixos;

            if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
                agendamentosFixos = agendamentoFixoRepository.findAll();
                logger.info("✅ ADMIN listando todos os {} agendamentos fixos", agendamentosFixos.size());
            } else {
                Profissional profissional = profissionalRepository.findByLogin(userDetails.getUsername());
                if (profissional == null) {
                    logger.warn("❌ Profissional não encontrado: {}", userDetails.getUsername());
                    return ResponseEntity.status(403).body("Profissional não encontrado.");
                }

                // Profissional vê apenas seus próprios agendamentos fixos
                agendamentosFixos = agendamentoFixoRepository.findByProfissional(profissional);
                logger.info("✅ PROFISSIONAL {} listando seus {} agendamentos fixos",
                        profissional.getNome(), agendamentosFixos.size());
            }

            return ResponseEntity.ok(agendamentosFixos);
        } catch (Exception e) {
            logger.error("❌ Erro ao listar agendamentos fixos", e);
            return ResponseEntity.status(500).body("Erro ao listar agendamentos fixos: " + e.getMessage());
        }
    }

    // ✅ Endpoint para gerar agendamentos com base nos fixos - versão adaptada para
    // o novo modelo
    @PostMapping("/fixo/gerar")
    public ResponseEntity<?> gerarAgendamentosFixos(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔍 Solicitação para gerar agendamentos fixos por: {}", userDetails.getUsername());

        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            logger.warn("❌ Tentativa de geração de agendamentos fixos sem permissão por {}", userDetails.getUsername());
            return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode gerar agendamentos fixos.");
        }

        try {
            LocalDate hoje = LocalDate.now();
            int agendamentosCriados = 0;

            // Processar agendamentos diários
            List<AgendamentoFixo> agendamentosDiarios = agendamentoFixoRepository.findActiveDailySchedules(hoje);
            for (AgendamentoFixo agendamentoFixo : agendamentosDiarios) {
                // Verificar se o intervalo bate com o dia de hoje
                if ((hoje.toEpochDay() - agendamentoFixo.getDataInicio().toEpochDay()) %
                        agendamentoFixo.getIntervaloRepeticao() == 0) {
                    criarAgendamentoAPartirDeFixo(agendamentoFixo, hoje);
                    agendamentosCriados++;
                }
            }

            // Processar agendamentos semanais (verificar dia da semana)
            int diaDaSemana = hoje.getDayOfWeek().getValue() % 7 + 1; // 1=domingo, 2=segunda... para padrão ISO: uso %
                                                                      // 7 + 1
            List<AgendamentoFixo> agendamentosSemanais = agendamentoFixoRepository.findActiveWeeklySchedulesForDay(hoje,
                    diaDaSemana);
            for (AgendamentoFixo agendamentoFixo : agendamentosSemanais) {
                // Verificar se o intervalo de semanas é o correto
                long semanasDesdoInicio = (hoje.toEpochDay() - agendamentoFixo.getDataInicio().toEpochDay()) / 7;
                if (semanasDesdoInicio % agendamentoFixo.getIntervaloRepeticao() == 0) {
                    criarAgendamentoAPartirDeFixo(agendamentoFixo, hoje);
                    agendamentosCriados++;
                }
            }

            // Processar agendamentos mensais (para dia específico do mês)
            int diaDoMes = hoje.getDayOfMonth();
            List<AgendamentoFixo> agendamentosMensais = agendamentoFixoRepository.findActiveMonthlySchedulesForDay(hoje,
                    diaDoMes);
            for (AgendamentoFixo agendamentoFixo : agendamentosMensais) {
                // Verificar o intervalo de meses
                int mesesDesdoInicio = hoje.getMonthValue() - agendamentoFixo.getDataInicio().getMonthValue() +
                        12 * (hoje.getYear() - agendamentoFixo.getDataInicio().getYear());
                if (mesesDesdoInicio % agendamentoFixo.getIntervaloRepeticao() == 0) {
                    criarAgendamentoAPartirDeFixo(agendamentoFixo, hoje);
                    agendamentosCriados++;
                }
            }

            // Verificar se é o último dia do mês
            boolean ultimoDiaDoMes = hoje.getDayOfMonth() == hoje.getMonth().length(hoje.isLeapYear());
            if (ultimoDiaDoMes) {
                // Processar agendamentos para o último dia do mês
                List<AgendamentoFixo> agendamentosUltimoDia = agendamentoFixoRepository
                        .findActiveMonthlySchedulesForLastDay(hoje);
                for (AgendamentoFixo agendamentoFixo : agendamentosUltimoDia) {
                    int mesesDesdoInicio = hoje.getMonthValue() - agendamentoFixo.getDataInicio().getMonthValue() +
                            12 * (hoje.getYear() - agendamentoFixo.getDataInicio().getYear());
                    if (mesesDesdoInicio % agendamentoFixo.getIntervaloRepeticao() == 0) {
                        criarAgendamentoAPartirDeFixo(agendamentoFixo, hoje);
                        agendamentosCriados++;
                    }
                }
            }

            logger.info("✅ Foram gerados {} agendamentos fixos com sucesso.", agendamentosCriados);
            return ResponseEntity.ok("Foram gerados " + agendamentosCriados + " agendamentos fixos com sucesso.");
        } catch (Exception e) {
            logger.error("❌ Erro ao gerar agendamentos fixos", e);
            return ResponseEntity.status(500).body("Erro ao gerar agendamentos fixos: " + e.getMessage());
        }
    }

    // Método auxiliar para criar um agendamento a partir de um agendamento fixo
    private void criarAgendamentoAPartirDeFixo(AgendamentoFixo agendamentoFixo, LocalDate data) {
        // Cria o agendamento
        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(agendamentoFixo.getCliente());
        agendamento.setProfissional(agendamentoFixo.getProfissional());
        agendamento.setServico(agendamentoFixo.getServico());
        agendamento.setData(data);
        agendamento.setHora(agendamentoFixo.getHora());
        agendamento.setDuracao(Duration.parse(agendamentoFixo.getDuracao()));
        agendamento.setObservacao(agendamentoFixo.getObservacao());
        agendamento.setValor(agendamentoFixo.getValor());

        agendamentoRepository.save(agendamento);
        logger.info("✅ Agendamento gerado a partir do agendamento fixo {}: {}", agendamentoFixo.getId(), agendamento);
    }

    // ✅ ADMIN vê todos os agendamentos, PROFISSIONAL vê apenas os seus
    @GetMapping
    public ResponseEntity<?> listarAgendamentos(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔍 Solicitando lista de agendamentos para: {}", userDetails.getUsername());

        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            logger.info("✅ ADMIN solicitou todos os agendamentos.");
            return ResponseEntity.ok(agendamentoRepository.findAll());
        } else {
            Profissional profissional = profissionalRepository.findByLogin(userDetails.getUsername());
            if (profissional == null) {
                logger.warn("❌ Profissional não encontrado: {}", userDetails.getUsername());
                return ResponseEntity.status(403).body("Profissional não encontrado.");
            }
            logger.info("✅ PROFISSIONAL {} solicitou seus agendamentos.", profissional.getNome());
            return ResponseEntity.ok(agendamentoRepository.findByProfissional(profissional));
        }
    }

    // ✅ NOVA ROTA: PROFISSIONAL pode ver apenas seus próprios agendamentos
    @GetMapping("/profissional")
    public ResponseEntity<?> listarAgendamentosProfissional(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔍 PROFISSIONAL {} solicitando seus agendamentos.", userDetails.getUsername());

        Profissional profissional = profissionalRepository.findByLogin(userDetails.getUsername());

        if (profissional == null) {
            logger.warn("❌ Profissional não encontrado.");
            return ResponseEntity.status(403).body("Profissional não encontrado.");
        }

        List<Agendamento> agendamentos = agendamentoRepository.findByProfissional(profissional);
        logger.info("✅ Retornando {} agendamentos para PROFISSIONAL {}", agendamentos.size(), profissional.getNome());
        return ResponseEntity.ok(agendamentos);
    }

    @GetMapping("/profissional/{id}")
    public ResponseEntity<?> listarAgendamentosPorProfissional(
            @PathVariable Long id,
            @RequestParam String dataInicio,
            @RequestParam String dataFim,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔍 Solicitando agendamentos para o profissional {} entre {} e {} por {}",
                id, dataInicio, dataFim, userDetails.getUsername());

        // Verifica se o usuário é ADMIN ou o próprio profissional
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            Profissional profissional = profissionalRepository.findByLogin(userDetails.getUsername());
            if (profissional == null || profissional.getId() != id.longValue()) {
                logger.warn("❌ Acesso negado para o profissional {}.", id);
                return ResponseEntity.status(403).body("Acesso negado.");
            }
        }

        try {
            LocalDate inicio = LocalDate.parse(dataInicio);
            LocalDate fim = LocalDate.parse(dataFim);

            logger.info("🔍 Parâmetros recebidos: profissionalId={}, dataInicio={}, dataFim={}", id, inicio, fim);

            List<Agendamento> agendamentos = agendamentoRepository.findByProfissionalAndDataBetween(
                    profissionalRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("Profissional não encontrado")),
                    inicio,
                    fim);

            logger.info("✅ Retornando {} agendamentos para o profissional {}.", agendamentos.size(), id);
            return ResponseEntity.ok(agendamentos);
        } catch (Exception e) {
            logger.error("❌ Erro ao buscar agendamentos", e);
            return ResponseEntity.status(500).body("Erro ao buscar agendamentos.");
        }
    }

    @GetMapping("/dia")
    public ResponseEntity<?> listarPorData(@RequestParam String data,

            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔍 Solicitando agendamentos para o dia {} por {}", data, userDetails.getUsername());

        try {
            // Parse da data para LocalDate
            LocalDate dataFormatada = LocalDate.parse(data);

            List<Agendamento> agendamentosNormais;
            List<AgendamentoFixo> agendamentosFixosDoDia;

            if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
                // ADMIN pode ver todos os agendamentos normais e fixos do dia
                agendamentosNormais = agendamentoRepository.findByData(dataFormatada);
                agendamentosFixosDoDia = agendamentoFixoRepository.findByDiaDoMes(dataFormatada.getDayOfMonth());
                logger.info("✅ ADMIN solicitou agendamentos para o dia {}: {} normais e {} fixos encontrados",
                        dataFormatada, agendamentosNormais.size(), agendamentosFixosDoDia.size());
            } else {
                // PROFISSIONAL pode ver apenas seus próprios agendamentos normais e fixos
                Profissional profissional = profissionalRepository.findByLogin(userDetails.getUsername());
                if (profissional == null) {
                    logger.warn("❌ Profissional não encontrado: {}", userDetails.getUsername());
                    return ResponseEntity.status(403).body("Profissional não encontrado.");
                }
                agendamentosNormais = agendamentoRepository.findByProfissionalAndData(profissional, dataFormatada);
                agendamentosFixosDoDia = agendamentoFixoRepository.findByProfissionalAndDiaDoMes(profissional,
                        dataFormatada.getDayOfMonth());

                logger.info("✅ PROFISSIONAL {} solicitou agendamentos para o dia {}: {} normais e {} fixos encontrados",
                        profissional.getNome(), dataFormatada, agendamentosNormais.size(),
                        agendamentosFixosDoDia.size());

            }

            // Combine os agendamentos normais e fixos em uma única resposta
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("agendamentosNormais", agendamentosNormais);
            resposta.put("agendamentosFixos", agendamentosFixosDoDia);

            return ResponseEntity.ok(resposta);
        } catch (Exception e) {
            logger.error("❌ Erro ao listar agendamentos para o dia {}", data, e);
            return ResponseEntity.status(500).body("Erro ao listar agendamentos.");
        }
    }

    @GetMapping("/metricas")
    public ResponseEntity<?> obterMetricas(
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataFim,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔍 Solicitando métricas de agendamentos por {} com intervalo de {} a {}",
                userDetails.getUsername(), dataInicio, dataFim);

        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            logger.warn("❌ Tentativa de acesso às métricas sem permissão por {}", userDetails.getUsername());
            return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode acessar métricas.");
        }

        try {
            // Substituir LocalDate.MIN e LocalDate.MAX por valores válidos para PostgreSQL
            LocalDate inicio = dataInicio != null && !dataInicio.isEmpty() ? LocalDate.parse(dataInicio)
                    : LocalDate.of(1900, 1, 1);
            LocalDate fim = dataFim != null && !dataFim.isEmpty() ? LocalDate.parse(dataFim)
                    : LocalDate.of(294276, 12, 31);

            logger.info("🔍 Intervalo de datas: {} a {}", inicio, fim);

            long totalAgendamentos = agendamentoRepository.countByDataBetween(inicio, fim);
            List<Object[]> agendamentosPorProfissional = agendamentoRepository
                    .countAgendamentosPorProfissionalBetween(inicio, fim);
            List<Object[]> agendamentosPorCliente = agendamentoRepository.countAgendamentosPorClienteBetween(inicio,
                    fim);
            List<Object[]> agendamentosPorData = agendamentoRepository.countAgendamentosPorDataBetween(inicio, fim);

            Map<String, Object> metricas = new HashMap<>();
            metricas.put("totalAgendamentos", totalAgendamentos);
            metricas.put("agendamentosPorProfissional", agendamentosPorProfissional);
            metricas.put("agendamentosPorCliente", agendamentosPorCliente);
            metricas.put("agendamentosPorData", agendamentosPorData);

            logger.info("✅ Métricas geradas com sucesso.");
            return ResponseEntity.ok(metricas);
        } catch (Exception e) {
            logger.error("❌ Erro ao gerar métricas", e);
            return ResponseEntity.status(500).body("Erro ao gerar métricas.");
        }
    }

    @GetMapping("/comissoes")
    public ResponseEntity<?> calcularComissoes(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔍 Solicitando cálculo de comissões por {}", userDetails.getUsername());

        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            logger.warn("❌ Tentativa de acesso às comissões sem permissão por {}", userDetails.getUsername());
            return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode acessar as comissões.");
        }

        try {
            List<Object[]> comissoes = agendamentoRepository.calcularComissaoPorProfissional(comissaoPercentual / 100);
            return ResponseEntity.ok(comissoes);
        } catch (Exception e) {
            logger.error("❌ Erro ao calcular comissões", e);
            return ResponseEntity.status(500).body("Erro ao calcular comissões.");
        }
    }

    @GetMapping("/comissoes/total/{id}")
    public ResponseEntity<?> calcularComissaoTotalPorPeriodo(
            @PathVariable Long id,
            @RequestParam String dataInicio,
            @RequestParam String dataFim,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔍 Solicitando cálculo de comissão total para o profissional {} entre {} e {} por {}",
                id, dataInicio, dataFim, userDetails.getUsername());

        // Verificar se o usuário é ADMIN ou se é o próprio profissional acessando seus
        // dados
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));
        boolean isProfissionalAcessandoPropriosDados = false;

        if (!isAdmin) {
            // Verificar se é o próprio profissional acessando seus dados
            Profissional profissional = profissionalRepository.findByLogin(userDetails.getUsername());
            if (profissional != null && profissional.getId() == id.longValue()) {
                isProfissionalAcessandoPropriosDados = true;
                logger.info("✅ Profissional {} acessando suas próprias comissões", userDetails.getUsername());
            }
        }

        if (!isAdmin && !isProfissionalAcessandoPropriosDados) {
            logger.warn("❌ Tentativa de acesso às comissões sem permissão por {}", userDetails.getUsername());
            return ResponseEntity.status(403).body("Acesso negado. Você pode acessar apenas suas próprias comissões.");
        }

        try {
            LocalDate inicio = LocalDate.parse(dataInicio);
            LocalDate fim = LocalDate.parse(dataFim);

            logger.info("🔍 Parâmetros recebidos: profissionalId={}, dataInicio={}, dataFim={}", id, inicio, fim);

            // 1. Calcular comissão dos agendamentos normais
            Double comissaoAgendamentosNormais = agendamentoRepository.calcularComissaoTotalPorPeriodo(
                    id, inicio, fim, comissaoPercentual / 100);

            if (comissaoAgendamentosNormais == null) {
                comissaoAgendamentosNormais = 0.0;
            }

            // 2. Buscar e calcular a comissão dos agendamentos fixos no período
            Double comissaoAgendamentosFixos = calcularComissaoAgendamentosFixos(id, inicio, fim);

            // 3. Somar as comissões
            Double comissaoTotal = comissaoAgendamentosNormais + comissaoAgendamentosFixos;

            logger.info("✅ Comissão total calculada: R$ {} (Agendamentos normais: R$ {}, Agendamentos fixos: R$ {})",
                    comissaoTotal, comissaoAgendamentosNormais, comissaoAgendamentosFixos);

            Map<String, Object> response = new HashMap<>();
            response.put("profissionalId", id);
            response.put("comissaoTotal", comissaoTotal);
            response.put("comissaoAgendamentosNormais", comissaoAgendamentosNormais);
            response.put("comissaoAgendamentosFixos", comissaoAgendamentosFixos);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Erro ao calcular comissão total", e);
            return ResponseEntity.status(500).body("Erro ao calcular comissão total: " + e.getMessage());
        }
    }

    // Novo endpoint para profissionais consultarem suas comissões
    @GetMapping("/minhas-comissoes")
    public ResponseEntity<?> consultarMinhasComissoes(
            @RequestParam String dataInicio,
            @RequestParam String dataFim,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔍 Profissional {} solicitando suas comissões entre {} e {}",
                userDetails.getUsername(), dataInicio, dataFim);

        try {
            // Buscar o profissional pelo nome de usuário
            Profissional profissional = profissionalRepository.findByLogin(userDetails.getUsername());

            if (profissional == null) {
                logger.warn("❌ Profissional não encontrado: {}", userDetails.getUsername());
                return ResponseEntity.status(403).body("Profissional não encontrado.");
            }

            // Formatar datas
            LocalDate inicio = LocalDate.parse(dataInicio);
            LocalDate fim = LocalDate.parse(dataFim);

            logger.info("🔍 Calculando comissões para profissional ID={}", profissional.getId());

            // 1. Calcular comissão dos agendamentos normais
            Double comissaoAgendamentosNormais = agendamentoRepository.calcularComissaoTotalPorPeriodo(
                    profissional.getId(), inicio, fim, comissaoPercentual / 100);

            if (comissaoAgendamentosNormais == null) {
                comissaoAgendamentosNormais = 0.0;
            }

            // 2. Buscar e calcular a comissão dos agendamentos fixos no período
            Double comissaoAgendamentosFixos = calcularComissaoAgendamentosFixos(profissional.getId(), inicio, fim);

            // 3. Somar as comissões
            Double comissaoTotal = comissaoAgendamentosNormais + comissaoAgendamentosFixos;

            logger.info("✅ Comissão total calculada para profissional {}: R$ {}",
                    profissional.getNome(), comissaoTotal);

            Map<String, Object> response = new HashMap<>();
            response.put("profissionalId", profissional.getId());
            response.put("nome", profissional.getNome());
            response.put("periodo", Map.of(
                    "inicio", dataInicio,
                    "fim", dataFim));
            response.put("comissaoTotal", comissaoTotal);
            response.put("comissaoAgendamentosNormais", comissaoAgendamentosNormais);
            response.put("comissaoAgendamentosFixos", comissaoAgendamentosFixos);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Erro ao calcular comissões do profissional", e);
            return ResponseEntity.status(500).body("Erro ao calcular comissões: " + e.getMessage());
        }
    }

    // ✅ ADMIN pode criar agendamentos para todos, PROFISSIONAL apenas para si
    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody DadosCadastroAgendamento dados,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔍 Solicitação para criar agendamento por: {}", userDetails.getUsername());

        try {
            boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));

            // Buscar o cliente
            Cliente cliente = clienteRepository.findById(dados.clienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

            // Buscar o serviço
            Servico servico = servicoRepository.findById(dados.servicoId())
                    .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

            Profissional profissional;

            if (isAdmin) {
                // Admin pode criar para qualquer profissional
                profissional = profissionalRepository.findById(dados.profissionalId())
                        .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));
                logger.info("✅ ADMIN criando agendamento para o profissional: {}", profissional.getNome());
            } else {
                // Profissional só pode criar para si mesmo
                profissional = profissionalRepository.findByLogin(userDetails.getUsername());
                if (profissional == null) {
                    logger.warn("❌ Profissional não encontrado para o usuário: {}", userDetails.getUsername());
                    return ResponseEntity.status(403).body("Profissional não encontrado.");
                }

                // Verificar se o profissionalId no request corresponde ao profissional logado
                if (profissional.getId() != dados.profissionalId()) {
                    logger.warn("❌ Profissional tentando criar agendamento para outro profissional: {}",
                            dados.profissionalId());
                    return ResponseEntity.status(403).body("Você só pode criar agendamentos para você mesmo.");
                }
                logger.info("✅ PROFISSIONAL {} criando agendamento para si mesmo", profissional.getNome());
            }

            // NOVO: Verificar se há bloqueios na agenda para este horário
            List<BloqueioAgenda> bloqueios = bloqueioRepository.findByProfissionalAndData(
                    profissional, dados.data());

            for (BloqueioAgenda bloqueio : bloqueios) {
                // Verificar se é bloqueio de dia todo
                if (bloqueio.isDiaTodo()) {
                    return ResponseEntity.badRequest().body(
                            "Este dia está bloqueado na agenda. Motivo: " + bloqueio.getMotivo());
                }

                // Verificar se o horário solicitado está dentro do período bloqueado
                LocalTime horaInicio = dados.hora();
                LocalTime horaFim = dados.hora().plus(Duration.parse(dados.duracao()));

                if ((horaInicio.isBefore(bloqueio.getHoraFim()) && horaFim.isAfter(bloqueio.getHoraInicio()))) {
                    return ResponseEntity.badRequest().body(
                            "Este horário está bloqueado na agenda. Motivo: " + bloqueio.getMotivo());
                }
            }

            // Converte a string duracao para Duration
            Duration duracao = Duration.parse(dados.duracao());

            // NOTA: A verificação de conflito de horário com outros agendamentos foi
            // removida
            // Agora permitimos agendamentos simultâneos

            // Cria o agendamento com o serviço
            Agendamento agendamento = new Agendamento(dados, cliente, profissional, servico);
            agendamento.setDuracao(duracao);
            agendamento.setValor(dados.valor());

            agendamentoRepository.save(agendamento);

            String usuarioTipo = isAdmin ? "ADMIN" : "PROFISSIONAL";
            logger.info("✅ Agendamento criado com sucesso por {}: {}", usuarioTipo, agendamento);
            return ResponseEntity.ok("Agendamento criado com sucesso.");
        } catch (Exception e) {
            logger.error("❌ Erro ao criar agendamento", e);
            return ResponseEntity.status(500).body("Erro ao criar agendamento: " + e.getMessage());
        }
    }

    // ✅ ADMIN pode atualizar qualquer agendamento, PROFISSIONAL apenas os seus
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarAgendamento(
            @PathVariable Long id,
            @RequestBody DadosCadastroAgendamento dados,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔍 Solicitação para atualizar agendamento ID {} por: {}", id, userDetails.getUsername());

        try {
            boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));

            // Verifica se o agendamento existe
            Agendamento agendamento = agendamentoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

            // Se não é admin, verificar se é o profissional deste agendamento
            if (!isAdmin) {
                Profissional profissional = profissionalRepository.findByLogin(userDetails.getUsername());
                if (profissional == null) {
                    logger.warn("❌ Profissional não encontrado para o usuário: {}", userDetails.getUsername());
                    return ResponseEntity.status(403).body("Profissional não encontrado.");
                }

                if (agendamento.getProfissional().getId() != profissional.getId()) {
                    logger.warn("❌ Profissional {} tentando atualizar agendamento de outro profissional: {}",
                            profissional.getId(), agendamento.getProfissional().getId());
                    return ResponseEntity.status(403).body("Você só pode atualizar seus próprios agendamentos.");
                }

                // Profissional não pode alterar para outro profissional
                if (!dados.profissionalId().equals(profissional.getId())) {
                    logger.warn("❌ Profissional tentando transferir agendamento para outro profissional: {}",
                            dados.profissionalId());
                    return ResponseEntity.status(403)
                            .body("Você não pode transferir o agendamento para outro profissional.");
                }

                logger.info("✅ PROFISSIONAL {} atualizando seu próprio agendamento", profissional.getNome());
            } else {
                logger.info("✅ ADMIN atualizando agendamento ID {}", id);
            }

            // Buscar cliente, profissional e serviço
            Cliente cliente = clienteRepository.findById(dados.clienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

            Profissional profissional = profissionalRepository.findById(dados.profissionalId())
                    .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

            Servico servico = servicoRepository.findById(dados.servicoId())
                    .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

            // Converte a string duracao para Duration
            Duration duracao = Duration.parse(dados.duracao());

            // NOTA: A verificação de conflito de horário foi removida
            // Agora permitimos agendamentos simultâneos

            agendamento.setCliente(cliente);
            agendamento.setProfissional(profissional);
            agendamento.setServico(servico);
            agendamento.setData(dados.data());
            agendamento.setHora(dados.hora());
            agendamento.setDuracao(duracao);
            agendamento.setObservacao(dados.observacao());
            agendamento.setValor(dados.valor());

            agendamentoRepository.save(agendamento);
            String usuarioTipo = isAdmin ? "ADMIN" : "PROFISSIONAL";
            logger.info("✅ Agendamento atualizado com sucesso por {}: {}", usuarioTipo, agendamento);
            return ResponseEntity.ok("Agendamento atualizado com sucesso.");
        } catch (Exception e) {
            logger.error("❌ Erro ao atualizar agendamento", e);
            return ResponseEntity.status(500).body("Erro ao atualizar agendamento: " + e.getMessage());
        }
    }

    // ✅ ADMIN pode excluir qualquer agendamento, PROFISSIONAL apenas os seus
    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluirAgendamento(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔍 Solicitação para excluir agendamento ID {} por: {}", id, userDetails.getUsername());

        try {
            boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));

            // Verifica se o agendamento existe
            if (!agendamentoRepository.existsById(id)) {
                logger.warn("❌ Agendamento não encontrado. ID: {}", id);
                return ResponseEntity.status(404).body("Agendamento não encontrado.");
            }

            Agendamento agendamento = agendamentoRepository.findById(id).get();

            // Se não é admin, verificar se é o profissional deste agendamento
            if (!isAdmin) {
                Profissional profissional = profissionalRepository.findByLogin(userDetails.getUsername());
                if (profissional == null) {
                    logger.warn("❌ Profissional não encontrado para o usuário: {}", userDetails.getUsername());
                    return ResponseEntity.status(403).body("Profissional não encontrado.");
                }

                if (agendamento.getProfissional().getId() != profissional.getId()) {
                    logger.warn("❌ Profissional {} tentando excluir agendamento de outro profissional: {}",
                            profissional.getId(), agendamento.getProfissional().getId());
                    return ResponseEntity.status(403).body("Você só pode excluir seus próprios agendamentos.");
                }

                logger.info("✅ PROFISSIONAL {} excluindo seu próprio agendamento", profissional.getNome());
            } else {
                logger.info("✅ ADMIN excluindo agendamento ID {}", id);
            }

            agendamentoRepository.deleteById(id);
            String usuarioTipo = isAdmin ? "ADMIN" : "PROFISSIONAL";
            logger.info("✅ Agendamento excluído com sucesso por {}. ID: {}", usuarioTipo, id);
            return ResponseEntity.ok("Agendamento excluído com sucesso.");
        } catch (Exception e) {
            logger.error("❌ Erro ao excluir agendamento", e);
            return ResponseEntity.status(500).body("Erro ao excluir agendamento: " + e.getMessage());
        }
    }
}
