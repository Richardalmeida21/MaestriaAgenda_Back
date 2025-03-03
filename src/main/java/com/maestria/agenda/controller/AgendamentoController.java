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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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

    // ✅ ADMIN vê todos os agendamentos, PROFISSIONAL vê apenas os seus
    @GetMapping
    public ResponseEntity<?> listarAgendamentos(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            return ResponseEntity.ok(agendamentoRepository.findAll()); // 🔹 ADMIN vê todos os agendamentos
        }

        Profissional profissional = profissionalRepository.findByLogin(userDetails.getUsername());
        if (profissional == null) {
            return ResponseEntity.status(403).body("Profissional não encontrado.");
        }
        
        return ResponseEntity.ok(agendamentoRepository.findByProfissional(profissional)); // 🔹 Profissional vê apenas os próprios agendamentos
    }

    // ✅ Apenas ADMIN pode criar agendamentos
    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody DadosCadastroAgendamento dados, @AuthenticationPrincipal UserDetails userDetails) {
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode criar agendamentos.");
        }

        try {
            Cliente cliente = clienteRepository.findById(dados.clienteId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

            Profissional profissional = profissionalRepository.findById(dados.profissionalId())
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

            Agendamento agendamento = new Agendamento(dados, cliente, profissional);
            agendamentoRepository.save(agendamento);
            return ResponseEntity.ok("Agendamento criado com sucesso.");
        } catch (Exception e) {
            logger.error("❌ Erro ao criar agendamento", e);
            return ResponseEntity.status(500).body("Erro ao criar agendamento.");
        }
    }

    // ✅ Apenas ADMIN pode editar um agendamento
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody DadosCadastroAgendamento dados, @AuthenticationPrincipal UserDetails userDetails) {
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode editar agendamentos.");
        }

        Optional<Agendamento> agendamentoOpt = agendamentoRepository.findById(id);
        if (agendamentoOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Agendamento não encontrado.");
        }

        Agendamento agendamento = agendamentoOpt.get();
        Cliente cliente = clienteRepository.findById(dados.clienteId()).orElse(null);
        Profissional profissional = profissionalRepository.findById(dados.profissionalId()).orElse(null);

        if (cliente == null || profissional == null) {
            return ResponseEntity.badRequest().body("Cliente ou profissional inválido.");
        }

        agendamento.atualizarDados(dados, cliente, profissional);
        agendamentoRepository.save(agendamento);
        return ResponseEntity.ok("Agendamento atualizado com sucesso.");
    }

    // ✅ Apenas ADMIN pode excluir um agendamento
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode excluir agendamentos.");
        }

        if (!agendamentoRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("Agendamento não encontrado.");
        }

        agendamentoRepository.deleteById(id);
        return ResponseEntity.ok("Agendamento removido com sucesso.");
    }
}
