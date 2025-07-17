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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Objects;

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

    @PostMapping("/fixo")
    public ResponseEntity<?> cadastrarAgendamentoFixo(
            @RequestBody DadosCadastroAgendamentoFixo dados,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
                return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode criar agendamentos fixos.");
            }

            Cliente cliente = clienteRepository.findById(dados.clienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
            Profissional profissional = profissionalRepository.findById(dados.profissionalId())
                    .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));
            Servico servico = servicoRepository.findById(dados.servicoId())
                    .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

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

            if (dados.tipoRepeticao() == AgendamentoFixo.TipoRepeticao.MENSAL) {
                agendamentoFixo.setDiaDoMes(dados.diaDoMes() != null ? dados.diaDoMes() : dados.valorRepeticao());
            } else {
                agendamentoFixo.setDiaDoMes(1);
            }

            agendamentoFixoRepository.save(agendamentoFixo);

            LocalDate dataFimGeracao = LocalDate.now().plusDays(30);
            LocalDate dataAtual = agendamentoFixo.getDataInicio().isAfter(LocalDate.now())
                    ? agendamentoFixo.getDataInicio()
                    : LocalDate.now();

            int ocorrenciasCriadas = 0;
            while (!dataAtual.isAfter(dataFimGeracao)) {
                if (!dataAtual.isBefore(agendamentoFixo.getDataInicio()) &&
                        (agendamentoFixo.getDataFim() == null || !dataAtual.isAfter(agendamentoFixo.getDataFim()))) {

                    boolean gerarOcorrencia = false;
                    switch (agendamentoFixo.getTipoRepeticao()) {
                        case DIARIA:
                            gerarOcorrencia = (dataAtual.toEpochDay() - agendamentoFixo.getDataInicio().toEpochDay())
                                    % agendamentoFixo.getIntervaloRepeticao() == 0;
                            break;
                        case SEMANAL:
                            int diaDaSemana = dataAtual.getDayOfWeek().getValue() % 7 + 1;
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
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            Profissional profissional = profissionalRepository.findByLogin(userDetails.getUsername());
            if (profissional == null || profissional.getId() != id.longValue()) {
                return ResponseEntity.status(403).body("Acesso negado.");
            }
        }

        try {
            Profissional profissional = profissionalRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

            List<Agendamento> normais = agendamentoRepository.findByProfissional(profissional);
            List<AgendamentoFixo> fixos = agendamentoFixoRepository.findByProfissional(profissional);

            List<Map<String, Object>> agendamentos = new ArrayList<>();
            for (Agendamento a : normais) {
                Map<String, Object> item = new HashMap<>();
                // Valida se o agendamento possui agendamentoFixoId
                item.put("isFixo", a.getAgendamentoFixoId() != null);
                item.put("agendamento", a);
                agendamentos.add(item);
            }
            for (AgendamentoFixo f : fixos) {
                Map<String, Object> item = new HashMap<>();
                item.put("isFixo", true);
                item.put("agendamento", f);
                agendamentos.add(item);
            }

            return ResponseEntity.ok(agendamentos);
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
            List<Map<String, Object>> resultado = agendamentosFixos.stream().map(fixo -> {
                Map<String, Object> map = new HashMap<>();
                map.put("isFixo", true);
                map.put("agendamento", fixo);
                return map;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            logger.error("❌ Erro ao listar agendamentos fixos", e);
            return ResponseEntity.status(500).body("Erro ao listar agendamentos fixos: " + e.getMessage());
        }
    }

    private boolean isHorarioBloqueado(Profissional profissional, LocalDate data, LocalTime hora) {
        List<BloqueioAgenda> bloqueios = bloqueioRepository.findByProfissionalAndData(profissional, data);
        for (BloqueioAgenda bloqueio : bloqueios) {
            if (bloqueio.isDiaTodo() ||
                    (!hora.isBefore(bloqueio.getHoraInicio()) && !hora.isAfter(bloqueio.getHoraFim()))) {
                return true;
            }
        }
        return false;
    }

    private void criarAgendamentoAPartirDeFixo(AgendamentoFixo agendamentoFixo, LocalDate data) {
        if (isHorarioBloqueado(agendamentoFixo.getProfissional(), data, agendamentoFixo.getHora())) {
            logger.info("Horário bloqueado para data {}. Ocorrência não criada.", data);
            return;
        }
        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(agendamentoFixo.getCliente());
        agendamento.setProfissional(agendamentoFixo.getProfissional());
        agendamento.setServico(agendamentoFixo.getServico());
        agendamento.setData(data);
        agendamento.setHora(agendamentoFixo.getHora());
        agendamento.setObservacao(agendamentoFixo.getObservacao());
        agendamento.setAgendamentoFixoId(agendamentoFixo.getId());
        agendamentoRepository.save(agendamento);
    }

    @PutMapping("/fixo/{id}")
    public ResponseEntity<?> atualizarAgendamentoFixo(
            @PathVariable Long id,
            @RequestBody DadosCadastroAgendamentoFixo dados,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
                return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode atualizar agendamentos fixos.");
            }

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

            LocalDate hoje = LocalDate.now();
            List<Agendamento> agendamentosGerados = agendamentoRepository.findByAgendamentoFixoId(id);

            List<Agendamento> agendamentosFuturos = agendamentosGerados.stream()
                    .filter(a -> !a.getData().isBefore(hoje))
                    .collect(Collectors.toList());

            logger.info("🔄 Atualizando {} agendamentos futuros gerados pelo agendamento fixo ID {}",
                    agendamentosFuturos.size(), id);

            for (Agendamento agendamento : agendamentosFuturos) {
                agendamento.setCliente(cliente);
                agendamento.setProfissional(profissional);
                agendamento.setServico(servico);
                agendamento.setHora(agendamentoFixo.getHora());
                agendamento.setObservacao(agendamentoFixo.getObservacao());
                agendamentoRepository.save(agendamento);
            }

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

            excluirAgendamentosGeradosPorFixo(id);

            agendamentoFixoRepository.delete(agendamentoFixo);
            logger.info("✅ Agendamento fixo deletado com sucesso: {}", agendamentoFixo);
            return ResponseEntity
                    .ok("Agendamento fixo e todos os seus agendamentos gerados foram deletados com sucesso.");
        } catch (Exception e) {
            logger.error("❌ Erro ao deletar agendamento fixo", e);
            return ResponseEntity.status(500).body("Erro ao deletar agendamento fixo: " + e.getMessage());
        }
    }

    private void excluirAgendamentosGeradosPorFixo(Long agendamentoFixoId) {
        try {
            List<Agendamento> agendamentosGerados = agendamentoRepository.findByAgendamentoFixoId(agendamentoFixoId);

            if (!agendamentosGerados.isEmpty()) {
                logger.info("🔍 Excluindo {} agendamentos gerados pelo agendamento fixo ID {}",
                        agendamentosGerados.size(), agendamentoFixoId);

                agendamentoRepository.deleteAll(agendamentosGerados);

                logger.info("✅ {} agendamentos gerados foram excluídos com sucesso", agendamentosGerados.size());
            } else {
                logger.info("ℹ️ Nenhum agendamento gerado encontrado para o agendamento fixo ID {}", agendamentoFixoId);
            }
        } catch (Exception e) {
            logger.error("❌ Erro ao excluir agendamentos gerados pelo agendamento fixo ID {}", agendamentoFixoId, e);
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<?> listarAgendamentos(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔍 Solicitando lista de agendamentos para: {}", userDetails.getUsername());
        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            logger.info("✅ ADMIN solicitou todos os agendamentos.");
            List<Agendamento> todos = agendamentoRepository.findAll();
            List<Map<String, Object>> resultado = todos.stream().map(a -> {
                Map<String, Object> map = new HashMap<>();
                map.put("isFixo", a.getAgendamentoFixoId() != null);
                map.put("agendamento", a);
                return map;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(resultado);
        } else {
            Profissional profissional = profissionalRepository.findByLogin(userDetails.getUsername());
            if (profissional == null) {
                logger.warn("❌ Profissional não encontrado: {}", userDetails.getUsername());
                return ResponseEntity.status(403).body("Profissional não encontrado.");
            }
            logger.info("✅ PROFISSIONAL {} solicitando seus agendamentos.", profissional.getNome());
            List<Agendamento> agendamentos = agendamentoRepository.findByProfissional(profissional);
            List<Map<String, Object>> resultado = agendamentos.stream().map(a -> {
                Map<String, Object> map = new HashMap<>();
                map.put("isFixo", a.getAgendamentoFixoId() != null);
                map.put("agendamento", a);
                return map;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(resultado);
        }
    }

    @GetMapping("/profissional")
    public ResponseEntity<?> listarAgendamentosProfissional(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔍 PROFISSIONAL {} solicitando seus agendamentos.", userDetails.getUsername());

        Profissional profissional = profissionalRepository.findByLogin(userDetails.getUsername());

        if (profissional == null) {
            logger.warn("❌ Profissional não encontrado.");
            return ResponseEntity.status(403).body("Profissional não encontrado.");
        }

        List<Agendamento> agendamentos = agendamentoRepository.findByProfissional(profissional);
        List<Map<String, Object>> resultado = agendamentos.stream().map(a -> {
            Map<String, Object> map = new HashMap<>();
            map.put("isFixo", a.getAgendamentoFixoId() != null);
            map.put("agendamento", a);
            return map;
        }).collect(Collectors.toList());
        logger.info("✅ Retornando {} agendamentos para PROFISSIONAL {}", resultado.size(), profissional.getNome());
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/profissional/{id}")
    public ResponseEntity<?> listarAgendamentosPorProfissional(
            @PathVariable Long id,
            @RequestParam String dataInicio,
            @RequestParam String dataFim,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔍 Solicitando agendamentos para o profissional {} entre {} e {} por {}",
                id, dataInicio, dataFim, userDetails.getUsername());

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

            List<Map<String, Object>> resultado = agendamentos.stream().map(a -> {
                Map<String, Object> map = new HashMap<>();
                map.put("isFixo", a.getAgendamentoFixoId() != null);
                map.put("agendamento", a);
                return map;
            }).collect(Collectors.toList());

            logger.info("✅ Retornando {} agendamentos para o profissional {}.", resultado.size(), id);
            return ResponseEntity.ok(resultado);
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
            LocalDate dataFormatada = LocalDate.parse(data);
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
            List<Map<String, Object>> normais = agendamentosNormais.stream().map(a -> {
                Map<String, Object> map = new HashMap<>();
                map.put("isFixo", a.getAgendamentoFixoId() != null);
                map.put("agendamento", a);
                return map;
            }).collect(Collectors.toList());

            List<AgendamentoFixo> fixedActive = agendamentoFixoRepository.findActiveSchedulesForDate(dataFormatada);
            if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
                Profissional profissional = profissionalRepository.findByLogin(userDetails.getUsername());
                fixedActive = fixedActive.stream()
                        .filter(f -> f.getProfissional().getId() == profissional.getId())
                        .collect(Collectors.toList());
            }
            List<Map<String, Object>> fixos = fixedActive.stream().filter(fix -> {
                int currentDay = dataFormatada.getDayOfMonth();
                int dayOfWeek = dataFormatada.getDayOfWeek().getValue() % 7 + 1;
                boolean isLastDayOfMonth = (currentDay == dataFormatada.lengthOfMonth());
                switch (fix.getTipoRepeticao()) {
                    case DIARIA:
                        return true;
                    case SEMANAL:
                        return (fix.getValorRepeticao() & (1 << (dayOfWeek - 1))) != 0;
                    case MENSAL:
                        if (fix.getValorRepeticao() == -1) {
                            return isLastDayOfMonth;
                        }
                        return fix.getDiaDoMes() == currentDay;
                    default:
                        return false;
                }
            }).map(f -> {
                Map<String, Object> map = new HashMap<>();
                map.put("isFixo", true);
                map.put("agendamento", f);
                return map;
            }).collect(Collectors.toList());

            Map<String, Object> resposta = new HashMap<>();
            resposta.put("agendamentosNormais", normais);
            resposta.put("agendamentosFixos", fixos);
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

    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody DadosCadastroAgendamento dados,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔍 Solicitação para criar agendamento por: {}", userDetails.getUsername());

        try {
            boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));

            Cliente cliente = clienteRepository.findById(dados.clienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
            Servico servico = servicoRepository.findById(dados.servicoId())
                    .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

            Profissional profissional;
            if (isAdmin) {
                profissional = profissionalRepository.findById(dados.profissionalId())
                        .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));
                logger.info("✅ ADMIN criando agendamento para o profissional: {}", profissional.getNome());
            } else {
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

            if (isHorarioBloqueado(profissional, dados.data(), dados.hora())) {
                logger.warn("Horário bloqueado para profissional {} na data {} e hora {}",
                        profissional.getNome(), dados.data(), dados.hora());
                return ResponseEntity.badRequest().body("Horário bloqueado.");
            }

            Agendamento agendamento = new Agendamento();
            agendamento.setCliente(cliente);
            agendamento.setProfissional(profissional);
            agendamento.setServico(servico);
            agendamento.setData(dados.data());
            agendamento.setHora(dados.hora());
            agendamento.setObservacao(dados.observacao());
            agendamento.setPago(false);
            agendamento.setFormaPagamento(null);
            agendamento.setDataPagamento(null);

            agendamentoRepository.save(agendamento);
            logger.info("✅ Agendamento criado com sucesso: {}", agendamento);
            return ResponseEntity.ok("Agendamento criado com sucesso.");
        } catch (Exception e) {
            logger.error("❌ Erro ao criar agendamento", e);
            return ResponseEntity.status(500).body("Erro ao criar agendamento: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarAgendamento(
            @PathVariable Long id,
            @RequestBody DadosCadastroAgendamento dados,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔍 Solicitação para atualizar agendamento ID {} por: {}", id, userDetails.getUsername());

        try {
            boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));

            Agendamento agendamento = agendamentoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

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

            Cliente cliente = clienteRepository.findById(dados.clienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

            Profissional profissional = profissionalRepository.findById(dados.profissionalId())
                    .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

            Servico servico = servicoRepository.findById(dados.servicoId())
                    .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

            agendamento.setCliente(cliente);
            agendamento.setProfissional(profissional);
            agendamento.setServico(servico);
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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluirAgendamento(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔍 Solicitação para excluir agendamento ID {} por: {}", id, userDetails.getUsername());

        try {
            boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));

            if (!agendamentoRepository.existsById(id)) {
                logger.warn("❌ Agendamento não encontrado. ID: {}", id);
                return ResponseEntity.status(404).body("Agendamento não encontrado.");
            }

            Agendamento agendamento = agendamentoRepository.findById(id).get();

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

    // Novo endpoint para dar baixa em agendamento
    @PutMapping("/{id}/baixa")
    public ResponseEntity<?> darBaixaEmAgendamento(
            @PathVariable Long id,
            @RequestParam("formaPagamento") PagamentoTipo formaPagamento,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔄 Solicitação de baixa no agendamento ID {} por {}", id, userDetails.getUsername());
        try {
            Agendamento agendamento = agendamentoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

            // Permitir apenas ADMIN ou o próprio profissional
            boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));
            if (!isAdmin) {
                Profissional profissional = profissionalRepository.findByLogin(userDetails.getUsername());
                if (profissional == null || agendamento.getProfissional() == null || !Objects.equals(profissional.getId(), agendamento.getProfissional().getId())) {
                    return ResponseEntity.status(403).body("Acesso negado. Você só pode dar baixa nos seus próprios agendamentos.");
                }
            }

            if (agendamento.getPago() != null && agendamento.getPago()) {
                return ResponseEntity.badRequest().body("Agendamento já está marcado como pago.");
            }

            agendamento.setPago(true);
            agendamento.setDataPagamento(java.time.LocalDateTime.now());
            agendamento.setFormaPagamento(formaPagamento);
            agendamentoRepository.save(agendamento);

            return ResponseEntity.ok("Baixa realizada com sucesso.");
        } catch (Exception e) {
            logger.error("Erro ao dar baixa no agendamento: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao dar baixa no agendamento: " + e.getMessage());
        }
    }

    // Endpoint para listar agendamentos do dia seguinte com autenticação por API Key (variável de ambiente)
    @GetMapping("/amanha")
    public ResponseEntity<?> listarAgendamentosAmanha(@RequestHeader(value = "X-API-KEY", required = false) String apiKey) {
        final String API_KEY_ESPERADA = System.getenv("API_KEY_AGENDAMENTO");
        if (API_KEY_ESPERADA == null) {
            logger.error("❌ Variável de ambiente API_KEY_AGENDAMENTO não configurada!");
            return ResponseEntity.status(500).body("API Key do agendamento não configurada no servidor");
        }
        if (apiKey == null || !apiKey.equals(API_KEY_ESPERADA)) {
            logger.warn("❌ API Key inválida ou ausente na requisição para /amanha");
            return ResponseEntity.status(401).body("Acesso não autorizado: API Key inválida");
        }
        logger.info("🔍 Solicitando agendamentos do dia seguinte via API Key");
        try {
            LocalDate amanha = LocalDate.now().plusDays(1);
            List<Agendamento> agendamentos = agendamentoRepository.findByData(amanha);
            List<Map<String, Object>> resultado = agendamentos.stream().map(a -> {
                Map<String, Object> map = new HashMap<>();
                if (a.getCliente() != null) {
                    map.put("cliente", a.getCliente().getNome());
                    map.put("telefone_cliente", a.getCliente().getTelefone());
                } else {
                    map.put("cliente", null);
                    map.put("telefone_cliente", null);
                }
                map.put("horario", a.getHora());
                map.put("servico", a.getServico() != null ? a.getServico().getNome() : null);
                map.put("profissional", a.getProfissional() != null ? a.getProfissional().getNome() : null);
                map.put("data", a.getData());
                return map;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            logger.error("❌ Erro ao listar agendamentos do dia seguinte", e);
            return ResponseEntity.status(500).body("Erro ao listar agendamentos do dia seguinte: " + e.getMessage());
        }
    }
}
