package com.maestria.agenda.controller;

import com.maestria.agenda.profissional.Profissional;
import com.maestria.agenda.profissional.ProfissionalRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profissional")
@CrossOrigin(origins = "*")
public class ProfissionalController {

    private final ProfissionalRepository profissionalRepository;

    public ProfissionalController(ProfissionalRepository profissionalRepository) {
        this.profissionalRepository = profissionalRepository;
    }

    // Apenas ADMIN pode cadastrar profissionais
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> cadastrarProfissional(@RequestBody Profissional profissional) {
        if (profissionalRepository.findByLogin(profissional.getLogin()) != null) {
            return ResponseEntity.badRequest().body("Login já cadastrado.");
        }

        return ResponseEntity.ok(profissionalRepository.save(profissional));
    }

    // Apenas ADMIN pode listar todos os profissionais
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<Profissional> listarProfissionais() {
        return profissionalRepository.findAll();
    }
}
