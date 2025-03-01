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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/agendamento")
@CrossOrigin(origins = "*")
public class AgendamentoController {

    private static final Logger logger = LoggerFactory.getLogger(AgendamentoController.class);

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final ProfissionalRepository profissionalRepository;

    public AgendamentoController(AgendamentoRepository agendamentoRepository, ClienteRepository clienteRepository, ProfissionalRepository profissionalRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.clienteRepository = clienteRepository;
        this.profissionalRepository = profissionalRepository;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()") // 🔒 Exige autenticação
    public ResponseEntity<?> cadastrar(@RequestBody DadosCadastroAgendamento dados) {
        try {
            logger.info("📌 Iniciando cadastro de agendamento");

            // 🔍 1. Buscar Cliente no banco (evita duplicação)
            Optional<Cliente> clienteOpt = clienteRepository.findById(dados.clienteId());
            if (clienteOpt.isEmpty()) {
                return ResponseEntity.status(400).body("❌ Cliente não encontrado.");
            }
            Cliente cliente = clienteOpt.get();
            logger.info("✅ Cliente encontrado: {}", cliente.getNome());

            // 🔍 2. Buscar Profissional no banco
            Optional<Profissional> profissionalOpt = profissionalRepository.findById(dados.profissionalId());
            if (profissionalOpt.isEmpty()) {
                return ResponseEntity.status(400).body("❌ Profissional não encontrado.");
            }
            Profissional profissional = profissionalOpt.get();
            logger.info("✅ Profissional encontrado: {}", profissional.getNome());

            // 🔍 3. Criar o Agendamento
            LocalDateTime dataHora = LocalDateTime.parse(dados.dataHora()); // 🕒 Converte dataHora corretamente
            Agendamento agendamento = new Agendamento(cliente, profissional, dados.servico(), dataHora);
            agendamentoRepository.save(agendamento);
            logger.info("✅ Agendamento salvo: {}", agendamento);

            return ResponseEntity.ok("✅ Agendamento criado com sucesso.");
        } catch (Exception e) {
            logger.error("❌ Erro ao criar agendamento", e);
            return ResponseEntity.status(500).body("Erro ao criar agendamento.");
        }
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()") // 🔒 Exige autenticação
    public ResponseEntity<List<Agendamento>> listarAgendamentos() {
        List<Agendamento> agendamentos = agendamentoRepository.findAll();
        return ResponseEntity.ok(agendamentos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()") // 🔒 Exige autenticação
    public ResponseEntity<Agendamento> buscarAgendamentoPorId(@PathVariable Long id) {
        return agendamentoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()") // 🔒 Exige autenticação
    public ResponseEntity<?> atualizarAgendamento(@PathVariable Long id, @RequestBody DadosCadastroAgendamento dados) {
        Optional<Agendamento> agendamentoOpt = agendamentoRepository.findById(id);

        if (agendamentoOpt.isPresent()) {
            Agendamento agendamento = agendamentoOpt.get();

            // Atualizar Cliente
            Optional<Cliente> clienteOpt = clienteRepository.findById(dados.clienteId());
            if (clienteOpt.isEmpty()) {
                return ResponseEntity.status(400).body("❌ Cliente não encontrado.");
            }
            Cliente cliente = clienteOpt.get();

            // Atualizar Profissional
            Optional<Profissional> profissionalOpt = profissionalRepository.findById(dados.profissionalId());
            if (profissionalOpt.isEmpty()) {
                return ResponseEntity.status(400).body("❌ Profissional não encontrado.");
            }
            Profissional profissional = profissionalOpt.get();

            // Atualizar dados do agendamento
            LocalDateTime dataHora = LocalDateTime.parse(dados.dataHora());
            agendamento.setCliente(cliente);
            agendamento.setProfissional(profissional);
            agendamento.setServico(dados.servico());
            agendamento.setDataHora(dataHora);

            agendamentoRepository.save(agendamento);
            return ResponseEntity.ok("✅ Agendamento atualizado com sucesso.");
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()") // 🔒 Exige autenticação
    public ResponseEntity<?> deletarAgendamento(@PathVariable Long id) {
        if (agendamentoRepository.existsById(id)) {
            agendamentoRepository.deleteById(id);
            return ResponseEntity.ok("✅ Agendamento deletado com sucesso.");
        }
        return ResponseEntity.status(404).body("❌ Agendamento não encontrado.");
    }
}
