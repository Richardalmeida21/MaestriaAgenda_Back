package com.maestria.agenda.controller;

import com.maestria.agenda.agendamento.Agendamento;
import com.maestria.agenda.agendamento.AgendamentoFixo;
import com.maestria.agenda.agendamento.AgendamentoFixoRepository;
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
import java.time.LocalTime; 
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

@RestController
@RequestMapping("/agendamento")
@CrossOrigin(origins = "*")
public class AgendamentoController {

    private static final Logger logger = LoggerFactory.getLogger(AgendamentoController.class);

    private final AgendamentoRepository agendamentoRepository;
    private final AgendamentoFixoRepository agendamentoFixoRepository;
    private final ClienteRepository clienteRepository;
    private final ProfissionalRepository profissionalRepository;

    @Value("${comissao.percentual}")
    private double comissaoPercentual;

    public AgendamentoController(AgendamentoRepository agendamentoRepository,
            AgendamentoFixoRepository agendamentoFixoRepository,
            ClienteRepository clienteRepository,
            ProfissionalRepository profissionalRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.agendamentoFixoRepository = agendamentoFixoRepository;
        this.clienteRepository = clienteRepository;
        this.profissionalRepository = profissionalRepository;
    }

    // ✅ Listar todos os agendamentos - ADMIN pode ver todos
@GetMapping
public ResponseEntity<?> listarTodos(@AuthenticationPrincipal UserDetails userDetails) {
    logger.info("🔍 Solicitando todos os agendamentos por {}", userDetails.getUsername());

    try {
        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            // ADMIN pode ver todos os agendamentos
            List<Agendamento> agendamentos = agendamentoRepository.findAll();
            logger.info("✅ ADMIN - Retornando {} agendamentos.", agendamentos.size());
            return ResponseEntity.ok(agendamentos);
        } else {
            // PROFISSIONAL pode ver apenas seus próprios agendamentos
            Profissional profissional = profissionalRepository.findByLogin(userDetails.getUsername());
            if (profissional == null) {
                logger.warn("❌ Profissional não encontrado: {}", userDetails.getUsername());
                return ResponseEntity.status(403).body("Profissional não encontrado.");
            }
            
            List<Agendamento> agendamentos = agendamentoRepository.findByProfissional(profissional);
            logger.info("✅ PROFISSIONAL {} - Retornando {} agendamentos", profissional.getNome(), agendamentos.size());
            return ResponseEntity.ok(agendamentos);
        }
    } catch (Exception e) {
        logger.error("❌ Erro ao listar agendamentos", e);
        return ResponseEntity.status(500).body("Erro ao listar agendamentos.");
    }
}

    // ✅ Endpoint para criar agendamentos fixos
@PostMapping("/fixo")
public ResponseEntity<?> cadastrarAgendamentoFixo(
        @RequestBody Map<String, Object> requestData,
        @AuthenticationPrincipal UserDetails userDetails) {
    if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
        logger.warn("❌ Tentativa de criação de agendamento fixo sem permissão por {}", userDetails.getUsername());
        return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode criar agendamentos fixos.");
    }

    try {
        logger.info("📥 Dados recebidos: {}", requestData);
        
        // Validações
        if (requestData.get("valor") == null) {
            return ResponseEntity.badRequest()
                    .body("Erro: O valor do agendamento fixo deve ser informado.");
        }
        
        if (requestData.get("clienteId") == null || requestData.get("profissionalId") == null) {
            return ResponseEntity.badRequest()
                    .body("Erro: Cliente e Profissional são obrigatórios.");
        }
        
        if (requestData.get("diaDoMes") == null) {
            return ResponseEntity.badRequest()
                    .body("Erro: Dia do mês é obrigatório.");
        }
        
        if (requestData.get("hora") == null) {
            return ResponseEntity.badRequest()
                    .body("Erro: Hora é obrigatória.");
        }
        
        if (requestData.get("duracao") == null) {
            return ResponseEntity.badRequest()
                    .body("Erro: Duração é obrigatória.");
        }
        
        if (requestData.get("servico") == null) {
            return ResponseEntity.badRequest()
                    .body("Erro: Serviço é obrigatório.");
        }

        // Buscar cliente e profissional
        Cliente cliente = clienteRepository.findById(Long.valueOf(requestData.get("clienteId").toString()))
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
                
        Profissional profissional = profissionalRepository.findById(Long.valueOf(requestData.get("profissionalId").toString()))
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

        // Criar agendamento fixo
        AgendamentoFixo agendamentoFixo = new AgendamentoFixo();
        agendamentoFixo.setCliente(cliente);
        agendamentoFixo.setProfissional(profissional);
        agendamentoFixo.setDiaDoMes(Integer.valueOf(requestData.get("diaDoMes").toString()));
        agendamentoFixo.setHora(LocalTime.parse(requestData.get("hora").toString()));
        agendamentoFixo.setDuracao(requestData.get("duracao").toString());
        agendamentoFixo.setObservacao(requestData.get("observacao") != null ? requestData.get("observacao").toString() : null);
        agendamentoFixo.setValor(Double.valueOf(requestData.get("valor").toString()));
        
        // Definir o serviço (usando a enum Servicos)
        try {
            agendamentoFixo.setServico(com.maestria.agenda.servicos.Servicos.valueOf(requestData.get("servico").toString()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Erro: Serviço inválido. Valores permitidos: " + 
                          Arrays.toString(com.maestria.agenda.servicos.Servicos.values()));
        }

        agendamentoFixoRepository.save(agendamentoFixo);
        logger.info("✅ Agendamento fixo criado com sucesso: {}", agendamentoFixo);
        return ResponseEntity.ok(agendamentoFixo);
    } catch (Exception e) {
        logger.error("❌ Erro ao criar agendamento fixo", e);
        return ResponseEntity.status(500).body("Erro ao criar agendamento fixo: " + e.getMessage());
    }
}

    // ✅ Endpoint para listar agendamentos fixos
    @GetMapping("/fixo")
    public ResponseEntity<?> listarAgendamentosFixos(@AuthenticationPrincipal UserDetails userDetails) {
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            logger.warn("❌ Tentativa de acesso a agendamentos fixos sem permissão por {}", userDetails.getUsername());
            return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode visualizar agendamentos fixos.");
        }

        List<AgendamentoFixo> agendamentosFixos = agendamentoFixoRepository.findAll();
        logger.info("✅ Listando {} agendamentos fixos.", agendamentosFixos.size());
        return ResponseEntity.ok(agendamentosFixos);
    }

    // ✅ Endpoint para gerar agendamentos com base nos fixos
    @PostMapping("/fixo/gerar")
    public ResponseEntity<?> gerarAgendamentosFixos(@AuthenticationPrincipal UserDetails userDetails) {
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            logger.warn("❌ Tentativa de geração de agendamentos fixos sem permissão por {}", userDetails.getUsername());
            return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode gerar agendamentos fixos.");
        }

        try {
            List<AgendamentoFixo> agendamentosFixos = agendamentoFixoRepository.findAll();
            LocalDate hoje = LocalDate.now();

            for (AgendamentoFixo agendamentoFixo : agendamentosFixos) {
                if (hoje.getDayOfMonth() == agendamentoFixo.getDiaDoMes()) {
                    // Verifica conflitos de horário
                    List<Agendamento> agendamentosExistentes = agendamentoRepository.findByProfissionalAndData(
                            agendamentoFixo.getProfissional(), hoje);

                    LocalTime horaInicio = agendamentoFixo.getHora();
                    LocalTime horaFim = horaInicio.plus(Duration.parse(agendamentoFixo.getDuracao()));

                    boolean conflito = agendamentosExistentes.stream().anyMatch(agendamento -> {
                        LocalTime existenteHoraInicio = agendamento.getHora();
                        LocalTime existenteHoraFim = existenteHoraInicio.plus(agendamento.getDuracao());
                        return horaInicio.isBefore(existenteHoraFim) && horaFim.isAfter(existenteHoraInicio);
                    });

                    if (!conflito) {
                        // Cria o agendamento
                        Agendamento agendamento = new Agendamento();
                        agendamento.setCliente(agendamentoFixo.getCliente());
                        agendamento.setProfissional(agendamentoFixo.getProfissional());
                        agendamento.setData(hoje);
                        agendamento.setHora(horaInicio);
                        agendamento.setDuracao(Duration.parse(agendamentoFixo.getDuracao()));
                        agendamento.setObservacao(agendamentoFixo.getObservacao());
                        agendamento.setValor(agendamentoFixo.getValor()); // Define o valor do agendamento fixo

                        agendamentoRepository.save(agendamento);
                        logger.info("✅ Agendamento gerado com sucesso: {}", agendamento);
                    } else {
                        logger.warn("⚠️ Conflito de horário ao gerar agendamento fixo para {}", agendamentoFixo);
                    }
                }
            }

            return ResponseEntity.ok("Agendamentos fixos gerados com sucesso.");
        } catch (Exception e) {
            logger.error("❌ Erro ao gerar agendamentos fixos", e);
            return ResponseEntity.status(500).body("Erro ao gerar agendamentos fixos.");
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
            if (profissional == null || profissional.getId() != id) {
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

    if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
        logger.warn("❌ Tentativa de acesso às comissões sem permissão por {}", userDetails.getUsername());
        return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode acessar as comissões.");
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

// Novo método para calcular comissão de agendamentos fixos
private Double calcularComissaoAgendamentosFixos(Long profissionalId, LocalDate inicio, LocalDate fim) {
    try {
        // Buscar o profissional
        Profissional profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));
        
        // Buscar todos os agendamentos fixos do profissional
        List<AgendamentoFixo> agendamentosFixos = agendamentoFixoRepository.findByProfissional(profissional);
        
        double comissaoTotal = 0.0;
        
        // Para cada agendamento fixo
        for (AgendamentoFixo agendamentoFixo : agendamentosFixos) {
            int diaDoMes = agendamentoFixo.getDiaDoMes();
            double valorAgendamento = agendamentoFixo.getValor();
            
            // Verificar cada mês no intervalo e contar quantas vezes o dia do mês ocorre
            LocalDate dataAtual = inicio;
            while (!dataAtual.isAfter(fim)) {
                if (dataAtual.getDayOfMonth() == diaDoMes) {
                    // Dia do agendamento fixo encontrado no período
                    comissaoTotal += valorAgendamento * (comissaoPercentual / 100);
                }
                dataAtual = dataAtual.plusDays(1);
            }
        }
        
        return comissaoTotal;
    } catch (Exception e) {
        logger.error("❌ Erro ao calcular comissão de agendamentos fixos", e);
        return 0.0;
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

    // ✅ Endpoint para excluir agendamentos fixos
@DeleteMapping("/fixo/{id}")
public ResponseEntity<?> excluirAgendamentoFixo(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails) {
    if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
        logger.warn("❌ Tentativa de exclusão de agendamento fixo sem permissão por {}", userDetails.getUsername());
        return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode excluir agendamentos fixos.");
    }

    try {
        if (!agendamentoFixoRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("Erro: Agendamento fixo não encontrado.");
        }

        agendamentoFixoRepository.deleteById(id);
        logger.info("✅ Agendamento fixo excluído com sucesso. ID: {}", id);
        return ResponseEntity.ok("Agendamento fixo excluído com sucesso.");
    } catch (Exception e) {
        logger.error("❌ Erro ao excluir agendamento fixo", e);
        return ResponseEntity.status(500).body("Erro ao excluir agendamento fixo.");
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
