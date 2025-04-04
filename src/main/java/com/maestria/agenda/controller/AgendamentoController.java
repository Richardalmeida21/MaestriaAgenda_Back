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
import com.maestria.agenda.financeiro.PagamentoTipo;
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

        try {
            // Validação de permissões (apenas ADMIN pode cadastrar agendamentos fixos)
            if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
                return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode criar agendamentos fixos.");
            }

            // Busca as entidades necessárias
            Cliente cliente = clienteRepository.findById(dados.clienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
            Profissional profissional = profissionalRepository.findById(dados.profissionalId())
                    .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));
            Servico servico = servicoRepository.findById(dados.servicoId())
                    .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

            // Cria e popula o agendamento fixo
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
            agendamentoFixo.setObservacao(dados.observacao());

            // Para agendamento mensal, se diaDoMes não foi informado, usamos valorRepeticao
            if (dados.tipoRepeticao() == AgendamentoFixo.TipoRepeticao.MENSAL && dados.diaDoMes() == null) {
                agendamentoFixo.setDiaDoMes(dados.valorRepeticao());
            } else {
                agendamentoFixo.setDiaDoMes(dados.diaDoMes());
            }

            // NOVO: Verifica se a forma de pagamento foi informada e atribui (normalizando
            // para uppercase)
            if (dados.formaPagamento() == null || dados.formaPagamento().isEmpty()) {
                return ResponseEntity.badRequest().body("Forma de pagamento é obrigatória.");
            }
            agendamentoFixo.setFormaPagamento(dados.formaPagamento().toUpperCase());

            agendamentoFixoRepository.save(agendamentoFixo);

            // Geração automática de ocorrências (ex: para os próximos 30 dias)
            LocalDate dataFimGeracao = LocalDate.now().plusDays(30);
            LocalDate dataAtual = agendamentoFixo.getDataInicio().isAfter(LocalDate.now())
                    ? agendamentoFixo.getDataInicio()
                    : LocalDate.now();

            int ocorrenciasCriadas = 0;
            while (!dataAtual.isAfter(dataFimGeracao)) {
                // Verifica se a data atual está dentro da validade do agendamento fixo
                if (!dataAtual.isBefore(agendamentoFixo.getDataInicio()) &&
                        (agendamentoFixo.getDataFim() == null || !dataAtual.isAfter(agendamentoFixo.getDataFim()))) {

                    boolean gerarOcorrencia = false;
                    switch (agendamentoFixo.getTipoRepeticao()) {
                        case DIARIA:
                            gerarOcorrencia = (dataAtual.toEpochDay() - agendamentoFixo.getDataInicio().toEpochDay())
                                    % agendamentoFixo.getIntervaloRepeticao() == 0;
                            break;
                        case SEMANAL:
                            int diaDaSemana = dataAtual.getDayOfWeek().getValue() % 7 + 1; // 1 = domingo
                            gerarOcorrencia = (agendamentoFixo.getValorRepeticao() & (1 << (diaDaSemana - 1))) != 0;
                            break;
                        case MENSAL:
                            if (agendamentoFixo.getValorRepeticao() == -1) {
                                gerarOcorrencia = dataAtual.getDayOfMonth() == dataAtual.lengthOfMonth();
                            } else {
                                gerarOcorrencia = agendamentoFixo.getDiaDoMes() == dataAtual.getDayOfMonth();
                            }
                            break;
                        case QUINZENAL:
                            long diasDesdeInicio = dataAtual.toEpochDay()
                                    - agendamentoFixo.getDataInicio().toEpochDay();
                            gerarOcorrencia = diasDesdeInicio % 15 == 0;
                            break;
                        default:
                            break;
                    }

                    if (gerarOcorrencia) {
                        // Ao criar a ocorrência, repassa também a forma de pagamento do agendamento
                        // fixo
                        criarAgendamentoAPartirDeFixo(agendamentoFixo, dataAtual);
                        ocorrenciasCriadas++;
                    }
                }
                dataAtual = dataAtual.plusDays(1);
            }

            logger.info("✅ Foram geradas {} ocorrências para o agendamento fixo.", ocorrenciasCriadas);
            return ResponseEntity.ok(agendamentoFixo);

        } catch (Exception e) {
            logger.error("❌ Erro ao criar agendamento fixo", e);
            return ResponseEntity.status(500).body("Erro ao criar agendamento fixo: " + e.getMessage());
        }
    }

    @GetMapping("/todos/{id}")
    public ResponseEntity<?> listarTodosAgendamentosPorProfissional(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        // Se o usuário não for ADMIN, só permite acessar se for o próprio profissional
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            Profissional profissional = profissionalRepository.findByLogin(userDetails.getUsername());
            // Se o id do profissional for um primitivo long, compare usando '!='
            if (profissional == null || profissional.getId() != id.longValue()) {
                return ResponseEntity.status(403).body("Acesso negado.");
            }
        }

        try {
            Profissional profissional = profissionalRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

            // Buscar agendamentos normais
            List<Agendamento> normais = agendamentoRepository.findByProfissional(profissional);

            // Buscar agendamentos fixos
            List<AgendamentoFixo> fixos = agendamentoFixoRepository.findByProfissional(profissional);

            // Monta a resposta unindo ambos os tipos
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("agendamentosNormais", normais);
            resposta.put("agendamentosFixos", fixos);

            return ResponseEntity.ok(resposta);
        } catch (Exception e) {
            logger.error("❌ Erro ao listar todos agendamentos para o profissional " + id, e);
            return ResponseEntity.status(500).body("Erro ao listar agendamentos: " + e.getMessage());
        }
    }

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

    // Método auxiliar para criar um agendamento a partir de um agendamento fixo
    private void criarAgendamentoAPartirDeFixo(AgendamentoFixo agendamentoFixo, LocalDate data) {
    Agendamento agendamento = new Agendamento();
    agendamento.setCliente(agendamentoFixo.getCliente());
    agendamento.setProfissional(agendamentoFixo.getProfissional());
    agendamento.setServico(agendamentoFixo.getServico());
    agendamento.setData(data);
    agendamento.setHora(agendamentoFixo.getHora());
    agendamento.setObservacao(agendamentoFixo.getObservacao());
    agendamento.setFormaPagamento(PagamentoTipo.valueOf(agendamentoFixo.getFormaPagamento()));
    // Novo: marca a ocorrência com o id do agendamento fixo
    agendamento.setAgendamentoFixoId(agendamentoFixo.getId());
    
    agendamentoRepository.save(agendamento);
    logger.info("✅ Agendamento gerado a partir do agendamento fixo {}: {}", agendamentoFixo.getId(), agendamento);
}

    @PutMapping("/fixo/{id}")
    public ResponseEntity<?> atualizarAgendamentoFixo(
            @PathVariable Long id,
            @RequestBody DadosCadastroAgendamentoFixo dados,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔍 Solicitação para atualizar agendamento fixo ID {} por: {}", id, userDetails.getUsername());

        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            logger.warn("❌ Tentativa de atualização sem permissão por {}", userDetails.getUsername());
            return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode atualizar agendamentos fixos.");
        }

        try {
            AgendamentoFixo agendamentoFixo = agendamentoFixoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Agendamento fixo não encontrado"));

            Cliente cliente = clienteRepository.findById(dados.clienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
            Profissional profissional = profissionalRepository.findById(dados.profissionalId())
                    .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));
            Servico servico = servicoRepository.findById(dados.servicoId())
                    .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

            agendamentoFixo.setCliente(cliente);
            agendamentoFixo.setProfissional(profissional);
            agendamentoFixo.setServico(servico);
            agendamentoFixo.setTipoRepeticao(dados.tipoRepeticao());
            agendamentoFixo.setIntervaloRepeticao(dados.intervaloRepeticao());
            agendamentoFixo.setValorRepeticao(dados.valorRepeticao());
            agendamentoFixo.setDataInicio(dados.dataInicio());
            agendamentoFixo.setDataFim(dados.dataFim());
            agendamentoFixo.setHora(dados.hora());
            agendamentoFixo.setObservacao(dados.observacao());

            if (dados.tipoRepeticao() == AgendamentoFixo.TipoRepeticao.MENSAL) {
                agendamentoFixo.setDiaDoMes(dados.diaDoMes() != null ? dados.diaDoMes() : dados.valorRepeticao());
            } else {
                agendamentoFixo.setDiaDoMes(1);
            }

            agendamentoFixoRepository.save(agendamentoFixo);
            logger.info("✅ Agendamento fixo atualizado com sucesso: {}", agendamentoFixo);
            return ResponseEntity.ok(agendamentoFixo);
        } catch (Exception e) {
            logger.error("❌ Erro ao atualizar agendamento fixo", e);
            return ResponseEntity.status(500).body("Erro ao atualizar agendamento fixo: " + e.getMessage());
        }
    }

    @DeleteMapping("/fixo/{id}")
    public ResponseEntity<?> deletarAgendamentoFixo(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔍 Solicitação para deletar agendamento fixo ID {} por: {}", id, userDetails.getUsername());

        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            logger.warn("❌ Tentativa de exclusão sem permissão por {}", userDetails.getUsername());
            return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode excluir agendamentos fixos.");
        }

        try {
            AgendamentoFixo agendamentoFixo = agendamentoFixoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Agendamento fixo não encontrado"));

            agendamentoFixoRepository.delete(agendamentoFixo);
            logger.info("✅ Agendamento fixo deletado com sucesso: {}", agendamentoFixo);
            return ResponseEntity.ok("Agendamento fixo deletado com sucesso.");
        } catch (Exception e) {
            logger.error("❌ Erro ao deletar agendamento fixo", e);
            return ResponseEntity.status(500).body("Erro ao deletar agendamento fixo: " + e.getMessage());
        }
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
            // Converte a data recebida para LocalDate
            LocalDate dataFormatada = LocalDate.parse(data);

            // Buscar os agendamentos normais
            List<Agendamento> agendamentosNormais;
            if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
                agendamentosNormais = agendamentoRepository.findByData(dataFormatada);
            } else {
                Profissional profissional = profissionalRepository.findByLogin(userDetails.getUsername());
                if (profissional == null) {
                    logger.warn("❌ Profissional não encontrado: {}", userDetails.getUsername());
                    return ResponseEntity.status(403).body("Profissional não encontrado.");
                }
                agendamentosNormais = agendamentoRepository.findByProfissionalAndData(profissional, dataFormatada);
            }

            // Busca todos os agendamentos fixos ativos para a data
            List<AgendamentoFixo> fixedActive = agendamentoFixoRepository.findActiveSchedulesForDate(dataFormatada);

            // Se o usuário não for ADMIN, filtra apenas os seus próprios fixos
            if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
                Profissional profissional = profissionalRepository.findByLogin(userDetails.getUsername());
                fixedActive = fixedActive.stream()
                        .filter(f -> f.getProfissional().getId() == profissional.getId())
                        .toList();
            }

            // Filtra os fixos de acordo com o tipo de recorrência
            int currentDay = dataFormatada.getDayOfMonth();
            int dayOfWeek = dataFormatada.getDayOfWeek().getValue() % 7 + 1; // 1 = domingo, 2 = segunda, etc.
            boolean isLastDayOfMonth = (currentDay == dataFormatada.lengthOfMonth());

            List<AgendamentoFixo> agendamentosFixosDoDia = fixedActive.stream().filter(fix -> {
                switch (fix.getTipoRepeticao()) {
                    case DIARIA:
                        return true;
                    case SEMANAL:
                        // Para agendamentos semanais, assume-se que 'valorRepeticao' representa um bit
                        // mask
                        return (fix.getValorRepeticao() & (1 << (dayOfWeek - 1))) != 0;
                    case MENSAL:
                        if (fix.getValorRepeticao() == -1) {
                            // Valor -1 indica o último dia do mês
                            return isLastDayOfMonth;
                        }
                        return fix.getDiaDoMes() == currentDay;
                    default:
                        return false;
                }
            }).toList();

            logger.info("✅ Agendamentos para o dia {}: {} normais e {} fixos encontrados",
                    dataFormatada, agendamentosNormais.size(), agendamentosFixosDoDia.size());

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

    // ✅ ADMIN pode criar agendamentos para todos, PROFISSIONAL apenas para si
    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody DadosCadastroAgendamento dados,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔍 Solicitação para criar agendamento por: {}", userDetails.getUsername());

        try {
            boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));

            // Buscar cliente e serviço
            Cliente cliente = clienteRepository.findById(dados.clienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
            Servico servico = servicoRepository.findById(dados.servicoId())
                    .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

            Profissional profissional;
            if (isAdmin) {
                // ADMIN pode criar para qualquer profissional
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
                if (profissional.getId() != dados.profissionalId()) {
                    logger.warn("❌ Profissional tentando criar agendamento para outro profissional: {}",
                            dados.profissionalId());
                    return ResponseEntity.status(403).body("Você só pode criar agendamentos para você mesmo.");
                }
                logger.info("✅ PROFISSIONAL {} criando agendamento para si mesmo", profissional.getNome());
            }

            // Conversão e validação da forma de pagamento
            PagamentoTipo pagamentoTipo = dados.formaPagamento();
            if (pagamentoTipo == null) {
                return ResponseEntity.badRequest().body("Forma de pagamento inválida. Opções válidas: " +
                        "CREDITO_1X até CREDITO_10X, DEBITO, PIX, DINHEIRO.");
            }

            // Criação do agendamento, incluindo o atributo formaPagamento
            Agendamento agendamento = new Agendamento();
            agendamento.setCliente(cliente);
            agendamento.setProfissional(profissional);
            agendamento.setServico(servico);
            agendamento.setData(dados.data());
            agendamento.setHora(dados.hora());
            agendamento.setObservacao(dados.observacao());
            agendamento.setFormaPagamento(pagamentoTipo);

            // Salvar o agendamento
            agendamentoRepository.save(agendamento);
            logger.info("✅ Agendamento criado com sucesso: {}", agendamento);
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

            // NOTA: A verificação de conflito de horário foi removida
            // Agora permitimos agendamentos simultâneos

            agendamento.setCliente(cliente);
            agendamento.setProfissional(profissional);
            agendamento.setServico(servico); // O serviço já contém valor e duração
            agendamento.setData(dados.data());
            agendamento.setHora(dados.hora());
            agendamento.setObservacao(dados.observacao());

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
