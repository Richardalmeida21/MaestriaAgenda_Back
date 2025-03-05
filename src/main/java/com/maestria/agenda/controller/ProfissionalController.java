package com.maestria.agenda.controller;

import com.maestria.agenda.profissional.Profissional;
import com.maestria.agenda.profissional.ProfissionalRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/profissional")
@CrossOrigin(origins = "*")
public class ProfissionalController {

    private final ProfissionalRepository profissionalRepository;

    public ProfissionalController(ProfissionalRepository profissionalRepository) {
        this.profissionalRepository = profissionalRepository;
    }

    // ✅ Rota pública para testar login de profissional
    @GetMapping("/teste-login/{login}")
    public ResponseEntity<String> testarLogin(@PathVariable String login) {
        Profissional profissional = profissionalRepository.findByLogin(login);
        if (profissional != null) {
            return ResponseEntity.ok("✅ Profissional encontrado: " + profissional.getId() + " - " + profissional.getNome());
        } else {
            return ResponseEntity.status(404).body("❌ Profissional não encontrado!");
        }
    }

    // ✅ ADMIN e PROFISSIONAIS podem visualizar todos os profissionais
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFISSIONAL')")
    public ResponseEntity<List<Profissional>> listarProfissionais() {
        return ResponseEntity.ok(profissionalRepository.findAll());
    }

    // ✅ Apenas ADMIN pode cadastrar profissionais
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Profissional> criarProfissional(@Valid @RequestBody Profissional profissional) {
        Profissional novoProfissional = profissionalRepository.save(profissional);
        return ResponseEntity.ok(novoProfissional);
    }

    // ✅ Apenas ADMIN pode excluir profissionais
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> deletarProfissional(@PathVariable Long id) {
        if (profissionalRepository.existsById(id)) {
            profissionalRepository.deleteById(id);
            return ResponseEntity.ok("✅ Profissional deletado!");
        }
        return ResponseEntity.status(404).body("❌ Profissional não encontrado!");
    }
}
