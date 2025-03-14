package com.maestria.agenda.controller;

import com.maestria.agenda.cliente.Cliente;
import com.maestria.agenda.cliente.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/cliente")
@CrossOrigin(origins = "*")
public class ClienteController {

    private final ClienteRepository clienteRepository;

    @Autowired
    public ClienteController(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @GetMapping
    public List<Cliente> getAllClientes() {
        return clienteRepository.findAll();
    }

   @GetMapping("/{id}")
public ResponseEntity<Cliente> getClienteById(@PathVariable Long id) {
    return clienteRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
}


   @PostMapping
public ResponseEntity<Cliente> createCliente(@RequestBody @Valid DadosCliente dados) {
    Cliente cliente = new Cliente();
    cliente.setNome(dados.nome());
    cliente.setEmail(dados.email());
    cliente.setTelefone(dados.telefone());

    Cliente savedCliente = clienteRepository.save(cliente);
    return ResponseEntity.ok(savedCliente);
}


  @PutMapping("/{id}")
public ResponseEntity<Cliente> updateCliente(@PathVariable Long id, @RequestBody @Valid DadosCliente dados) {
    Optional<Cliente> existingCliente = clienteRepository.findById(id);

    if (existingCliente.isPresent()) {
        Cliente cliente = existingCliente.get();
        cliente.setNome(dados.nome());
        cliente.setEmail(dados.email());
        cliente.setTelefone(dados.telefone());

        Cliente savedCliente = clienteRepository.save(cliente);
        return ResponseEntity.ok(savedCliente);
    } else {
        return ResponseEntity.status(404).build();
    }
}



    @DeleteMapping("/{id}")
public ResponseEntity<Void> deleteCliente(@PathVariable Long id) {
    if (!clienteRepository.existsById(id)) {
        return ResponseEntity.notFound().build();
    }
    clienteRepository.deleteById(id);
    return ResponseEntity.noContent().build(); 
}

    }
}
