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
            logger.info("✅ PROFISSIONAL solicitou seus próprios agendamentos.");
            return ResponseEntity.ok(agendamentoRepository.findByProfissional(profissional));
        }
    }

    // ✅ Apenas ADMIN pode criar agendamentos
    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody DadosCadastroAgendamento dados, @AuthenticationPrincipal UserDetails userDetails) {
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            logger.warn("❌ Tentativa de criação de agendamento sem permissão por {}", userDetails.getUsername());
            return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode criar agendamentos.");
        }

        // 🔍 Verifica se clienteId e profissionalId não são nulos
        if (dados.clienteId() == null || dados.profissionalId() == null) {
            return ResponseEntity.badRequest().body("Erro: Cliente e Profissional devem ser informados.");
        }

        try {
            Cliente cliente = clienteRepository.findById(dados.clienteId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

            Profissional profissional = profissionalRepository.findById(dados.profissionalId())
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

            Agendamento agendamento = new Agendamento(dados, cliente, profissional);
            agendamentoRepository.save(agendamento);
            logger.info("✅ Agendamento criado com sucesso: {}", agendamento);
            return ResponseEntity.ok("Agendamento criado com sucesso.");
        } catch (Exception e) {
            logger.error("❌ Erro ao criar agendamento", e);
            return ResponseEntity.status(500).body("Erro ao criar agendamento.");
        }
    }
}
