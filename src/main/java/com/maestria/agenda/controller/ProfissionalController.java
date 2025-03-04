package com.maestria.agenda.controller;

import com.maestria.agenda.profissional.Profissional;  // Certifique-se de que essa importação está correta
import com.maestria.agenda.profissional.ProfissionalRepository;
import org.springframework.http.ResponseEntity;  // Importação correta do ResponseEntity
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

    // Método para testar login do profissional
    @GetMapping("/teste-login/{login}")
    public ResponseEntity<String> testarLogin(@PathVariable String login) {
        Profissional profissional = profissionalRepository.findByLogin(login);
        if (profissional != null) {
            return ResponseEntity.ok("✅ Profissional encontrado: " + profissional.getId() + " - " + profissional.getNome());
        } else {
            return ResponseEntity.status(404).body("❌ Profissional não encontrado!");
        }
    }

    // Outros métodos
}
