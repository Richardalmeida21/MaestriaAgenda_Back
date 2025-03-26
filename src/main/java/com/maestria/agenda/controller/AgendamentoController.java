package com.maestria.agenda.controller;

import com.maestria.agenda.agendamento.Agendamento;
import com.maestria.agenda.agendamento.AgendamentoRepository;
import com.maestria.agenda.agendamento.DadosCadastroAgendamento;
import com.maestria.agenda.cliente.Cliente;
import com.maestria.agenda.cliente.ClienteRepository;
import com.maestria.agenda.profissional.Profissional;
import com.maestria.agenda.profissional.ProfissionalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalTime; // Import adicionado aqui
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agendamento")
@CrossOrigin(origins = "*")
public class AgendamentoController {

    private static final Logger logger = LoggerFactory.getLogger(AgendamentoController.class);

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final ProfissionalRepository profissionalRepository;

    @Value("${comissao.percentual}")
    private double comissaoPercentual;

    public AgendamentoController(AgendamentoRepository agendamentoRepository, ClienteRepository clienteRepository,
            ProfissionalRepository profissionalRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.clienteRepository = clienteRepository;
        this.profissionalRepository = profissionalRepository;
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

    // ✅ NOVA ROTA: Listar agendamentos por data
    @GetMapping("/dia")
    public ResponseEntity<?> listarPorData(@RequestParam String data,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔍 Solicitando agendamentos para o dia {} por {}", data, userDetails.getUsername());

        // Parse da data para LocalDate
        LocalDate dataFormatada = LocalDate.parse(data);

        List<Agendamento> agendamentos;

        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            // ADMIN pode ver todos os agendamentos
            agendamentos = agendamentoRepository.findByData(dataFormatada);
            logger.info("🔍 ADMIN solicitou agendamentos para o dia {}: {}", dataFormatada, agendamentos.size());
        } else {
            // PROFISSIONAL pode ver apenas seus próprios agendamentos
            Profissional profissional = profissionalRepository.findByLogin(userDetails.getUsername());
            if (profissional == null) {
                logger.warn("❌ Profissional não encontrado: {}", userDetails.getUsername());
                return ResponseEntity.status(403).body("Profissional não encontrado.");
            }
            agendamentos = agendamentoRepository.findByProfissionalAndData(profissional, dataFormatada);
            logger.info("🔍 PROFISSIONAL {} solicitou agendamentos para o dia {}: {}", profissional.getNome(),
                    dataFormatada, agendamentos.size());
        }

        return ResponseEntity.ok(agendamentos);
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
        LocalDate inicio = dataInicio != null && !dataInicio.isEmpty() ? LocalDate.parse(dataInicio) : LocalDate.of(1900, 1, 1);
        LocalDate fim = dataFim != null && !dataFim.isEmpty() ? LocalDate.parse(dataFim) : LocalDate.of(294276, 12, 31);

        logger.info("🔍 Intervalo de datas: {} a {}", inicio, fim);

        long totalAgendamentos = agendamentoRepository.countByDataBetween(inicio, fim);
        List<Object[]> agendamentosPorProfissional = agendamentoRepository.countAgendamentosPorProfissionalBetween(inicio, fim);
        List<Object[]> agendamentosPorCliente = agendamentoRepository.countAgendamentosPorClienteBetween(inicio, fim);
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

    if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
        logger.warn("❌ Tentativa de acesso às comissões sem permissão por {}", userDetails.getUsername());
        return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode acessar as comissões.");
    }

    try {
        LocalDate inicio = LocalDate.parse(dataInicio);
        LocalDate fim = LocalDate.parse(dataFim);

        logger.info("🔍 Parâmetros recebidos: profissionalId={}, dataInicio={}, dataFim={}", id, inicio, fim);

        Double comissaoTotal = agendamentoRepository.calcularComissaoTotalPorPeriodo(
                id, inicio, fim, comissaoPercentual / 100);

        if (comissaoTotal == null) {
            comissaoTotal = 0.0; 
        }

        logger.info("✅ Comissão total calculada: R$ {}", comissaoTotal);
        return ResponseEntity.ok(Map.of("profissionalId", id, "comissaoTotal", comissaoTotal));
    } catch (Exception e) {
        logger.error("❌ Erro ao calcular comissão total", e);
        return ResponseEntity.status(500).body("Erro ao calcular comissão total.");
    }
}

    // ✅ Apenas ADMIN pode criar agendamentos
    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody DadosCadastroAgendamento dados,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            logger.warn("❌ Tentativa de criação de agendamento sem permissão por {}", userDetails.getUsername());
            return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode criar agendamentos.");
        }

        if (dados.clienteId() == null || dados.profissionalId() == null) {
            return ResponseEntity.badRequest().body("Erro: Cliente e Profissional devem ser informados.");
        }

        try {
            Cliente cliente = clienteRepository.findById(dados.clienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

            Profissional profissional = profissionalRepository.findById(dados.profissionalId())
                    .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

            // Converte a string duracao para Duration
            Duration duracao = Duration.parse(dados.duracao());

            // Verifica conflitos de horário
            List<Agendamento> agendamentosExistentes = agendamentoRepository.findByProfissionalAndData(profissional,
                    dados.data());

            LocalTime horaInicio = dados.hora();
            LocalTime horaFim = horaInicio.plus(duracao);

            for (Agendamento agendamentoExistente : agendamentosExistentes) {
                LocalTime existenteHoraInicio = agendamentoExistente.getHora();
                LocalTime existenteHoraFim = existenteHoraInicio.plus(agendamentoExistente.getDuracao());

                // Verifica se há sobreposição de horários
                if (horaInicio.isBefore(existenteHoraFim) && horaFim.isAfter(existenteHoraInicio)) {
                    return ResponseEntity.badRequest()
                            .body("Conflito de horário: Já existe um agendamento para este horário.");
                }
            }

            // Cria o agendamento
            Agendamento agendamento = new Agendamento(dados, cliente, profissional);
            agendamento.setDuracao(duracao);
            agendamento.setValor(dados.valor());

            agendamentoRepository.save(agendamento);
            logger.info("✅ Agendamento criado com sucesso: {}", agendamento);
            return ResponseEntity.ok("Agendamento criado com sucesso.");
        } catch (Exception e) {
            logger.error("❌ Erro ao criar agendamento", e);
            return ResponseEntity.status(500).body("Erro ao criar agendamento.");
        }
    }

    // ✅ Apenas ADMIN pode atualizar agendamentos
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarAgendamento(
            @PathVariable Long id,
            @RequestBody DadosCadastroAgendamento dados,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            logger.warn("❌ Tentativa de atualização de agendamento sem permissão por {}", userDetails.getUsername());
            return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode atualizar agendamentos.");
        }

        // Verifica se o agendamento existe
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        // Atualiza os dados do agendamento
        try {
            Cliente cliente = clienteRepository.findById(dados.clienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

            Profissional profissional = profissionalRepository.findById(dados.profissionalId())
                    .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

            // Converte a string duracao para Duration
            Duration duracao = Duration.parse(dados.duracao());

            // Verifica conflitos de horário (exceto o próprio agendamento que está sendo
            // atualizado)
            List<Agendamento> agendamentosExistentes = agendamentoRepository
                    .findByProfissionalAndData(profissional, dados.data())
                    .stream()
                    .filter(a -> !a.getId().equals(id)) // Funciona se `a.getId()` retornar `Long`
                    .toList();

            LocalTime horaInicio = dados.hora();
            LocalTime horaFim = horaInicio.plus(duracao);

            for (Agendamento agendamentoExistente : agendamentosExistentes) {
                LocalTime existenteHoraInicio = agendamentoExistente.getHora();
                LocalTime existenteHoraFim = existenteHoraInicio.plus(agendamentoExistente.getDuracao());

                // Verifica se há sobreposição de horários
                if (horaInicio.isBefore(existenteHoraFim) && horaFim.isAfter(existenteHoraInicio)) {
                    return ResponseEntity.badRequest()
                            .body("Conflito de horário: Já existe um agendamento para este horário.");
                }
            }

            agendamento.setCliente(cliente);
            agendamento.setProfissional(profissional);
            agendamento.setServico(dados.servico());
            agendamento.setData(dados.data());
            agendamento.setHora(dados.hora());
            agendamento.setDuracao(duracao); // Define a duração convertida
            agendamento.setObservacao(dados.observacao());

            agendamentoRepository.save(agendamento);
            logger.info("✅ Agendamento atualizado com sucesso: {}", agendamento);
            return ResponseEntity.ok("Agendamento atualizado com sucesso.");
        } catch (Exception e) {
            logger.error("❌ Erro ao atualizar agendamento", e);
            return ResponseEntity.status(500).body("Erro ao atualizar agendamento.");
        }
    }

    // ✅ Apenas ADMIN pode excluir agendamentos
    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluirAgendamento(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            logger.warn("❌ Tentativa de exclusão de agendamento sem permissão por {}", userDetails.getUsername());
            return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode excluir agendamentos.");
        }

        if (!agendamentoRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("Erro: Agendamento não encontrado.");
        }

        agendamentoRepository.deleteById(id);
        logger.info("✅ Agendamento excluído com sucesso. ID: {}", id);
        return ResponseEntity.ok("Agendamento excluído com sucesso.");
    }
}
