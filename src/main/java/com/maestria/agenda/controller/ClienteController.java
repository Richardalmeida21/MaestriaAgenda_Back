package com.maestria.agenda.controller;

import com.maestria.agenda.agendamento.Agendamento;
import com.maestria.agenda.agendamento.AgendamentoRepository;
import com.maestria.agenda.cliente.Cliente;
import com.maestria.agenda.cliente.ClienteRepository;
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

    private final ClienteRepository clienteRepository;
    private final AgendamentoRepository agendamentoRepository;

    public ClienteController(ClienteRepository clienteRepository, AgendamentoRepository agendamentoRepository) {
        this.clienteRepository = clienteRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    // 🔹 ADMIN pode cadastrar um novo cliente
    @PostMapping
    public ResponseEntity<?> cadastrarCliente(@RequestBody Cliente cliente, @AuthenticationPrincipal UserDetails userDetails) {
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode criar clientes.");
        }
        Cliente novoCliente = clienteRepository.save(cliente);
        return ResponseEntity.ok(novoCliente);
    }

    // 🔹 ADMIN e PROFISSIONAIS podem visualizar os clientes
    @GetMapping
    public ResponseEntity<List<Cliente>> listarClientes(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN")) ||
            userDetails.getAuthorities().contains(new SimpleGrantedAuthority("PROFISSIONAL"))) {
            return ResponseEntity.ok(clienteRepository.findAll());
        }
        return ResponseEntity.status(403).build();
    }

    // 🔹 ADMIN pode buscar um cliente por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarClientePorId(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            return ResponseEntity.status(403).body("Acesso negado.");
        }
        Optional<Cliente> cliente = clienteRepository.findById(id);
        return cliente.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 🔹 ADMIN pode editar um cliente
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarCliente(@PathVariable Long id, @RequestBody Cliente cliente, @AuthenticationPrincipal UserDetails userDetails) {
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            return ResponseEntity.status(403).body("Acesso negado.");
        }
        if (clienteRepository.existsById(id)) {
            cliente.setId(id);
            Cliente atualizado = clienteRepository.save(cliente);
            return ResponseEntity.ok(atualizado);
        }
        return ResponseEntity.notFound().build();
    }

    // 🔹 ADMIN pode excluir um cliente (desde que ele não tenha agendamentos)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletarCliente(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            return ResponseEntity.status(403).body("Acesso negado.");
        }

        Optional<Cliente> clienteOptional = clienteRepository.findById(id);
        if (clienteOptional.isPresent()) {
            Cliente cliente = clienteOptional.get();

            // 🔍 Verifica se o cliente tem agendamentos antes de excluir
            List<Agendamento> agendamentos = agendamentoRepository.findByCliente(cliente);
            if (!agendamentos.isEmpty()) {
                return ResponseEntity.status(400).body("Não é possível excluir o cliente, pois ele tem agendamentos.");
            }

            clienteRepository.deleteById(id);
            return ResponseEntity.ok("Cliente removido com sucesso.");
        }
        return ResponseEntity.notFound().build();
    }
}
