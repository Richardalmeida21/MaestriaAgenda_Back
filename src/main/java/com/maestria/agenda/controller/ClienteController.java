package com.maestria.agenda.controller;

import com.maestria.agenda.agendamento.Agendamento;
import com.maestria.agenda.agendamento.AgendamentoRepository;
import com.maestria.agenda.cliente.Cliente;
import com.maestria.agenda.cliente.ClienteRepository;
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
@RequestMapping("/cliente")
@CrossOrigin(origins = "*")
public class ClienteController {

    private static final Logger logger = LoggerFactory.getLogger(ClienteController.class);

    private final ClienteRepository clienteRepository;
    private final AgendamentoRepository agendamentoRepository;

    public ClienteController(ClienteRepository clienteRepository, AgendamentoRepository agendamentoRepository) {
        this.clienteRepository = clienteRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    // ✅ 🔓 ADMIN e PROFISSIONAL podem listar clientes
    @GetMapping
    public ResponseEntity<?> listarClientes(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔍 Solicitando lista de clientes para: {}", userDetails.getUsername());
        return ResponseEntity.ok(clienteRepository.findAll());
    }

    // ✅ 🔒 Apenas ADMIN pode cadastrar clientes
    @PostMapping
    public ResponseEntity<?> cadastrarCliente(@RequestBody Cliente cliente, @AuthenticationPrincipal UserDetails userDetails) {
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            logger.warn("❌ Tentativa de criar cliente sem permissão por {}", userDetails.getUsername());
            return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode criar clientes.");
        }

        clienteRepository.save(cliente);
        logger.info("✅ Cliente cadastrado com sucesso: {}", cliente);
        return ResponseEntity.ok("Cliente cadastrado com sucesso.");
    }

    // ✅ 🔒 Apenas ADMIN pode editar clientes
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarCliente(@PathVariable Long id, @RequestBody Cliente cliente, @AuthenticationPrincipal UserDetails userDetails) {
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            logger.warn("❌ Tentativa de editar cliente sem permissão por {}", userDetails.getUsername());
            return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode editar clientes.");
        }

        if (!clienteRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("Cliente não encontrado.");
        }

        cliente.setId(id);
        clienteRepository.save(cliente);
        logger.info("✅ Cliente atualizado com sucesso: {}", cliente);
        return ResponseEntity.ok("Cliente atualizado com sucesso.");
    }

    // ✅ 🔒 Apenas ADMIN pode excluir clientes
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarCliente(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            logger.warn("❌ Tentativa de deletar cliente sem permissão por {}", userDetails.getUsername());
            return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode excluir clientes.");
        }

        Optional<Cliente> clienteOptional = clienteRepository.findById(id);
        if (clienteOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Cliente não encontrado.");
        }

        Cliente cliente = clienteOptional.get();
        List<Agendamento> agendamentos = agendamentoRepository.findByCliente(cliente);
        if (!agendamentos.isEmpty()) {
            return ResponseEntity.status(400).body("Erro: Cliente possui agendamentos e não pode ser excluído.");
        }

        clienteRepository.deleteById(id);
        logger.info("✅ Cliente excluído com sucesso. ID: {}", id);
        return ResponseEntity.ok("Cliente excluído com sucesso.");
    }
}
