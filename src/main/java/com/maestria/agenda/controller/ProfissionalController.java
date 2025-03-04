package com.maestria.agenda.controller;

import com.maestria.agenda.profissional.Profissional;
import com.maestria.agenda.profissional.ProfissionalRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/profissional")
@CrossOrigin(origins = "*")
public class ProfissionalController {

    private final ProfissionalRepository profissionalRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfissionalController(ProfissionalRepository profissionalRepository, PasswordEncoder passwordEncoder) {
        this.profissionalRepository = profissionalRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Apenas ADMIN pode cadastrar profissionais
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> cadastrarProfissional(@RequestBody Profissional profissional) {
        if (profissionalRepository.findByLogin(profissional.getLogin()) != null) {
            return ResponseEntity.badRequest().body("Login já cadastrado.");
        }

        profissional.setSenha(passwordEncoder.encode(profissional.getSenha())); // Criptografa senha
        return ResponseEntity.ok(profissionalRepository.save(profissional));
    }

    // Apenas ADMIN pode listar todos os profissionais
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<Profissional> listarProfissionais() {
        return profissionalRepository.findAll();
    }
}
