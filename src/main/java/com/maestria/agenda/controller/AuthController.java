package com.maestria.agenda.controller;

import com.maestria.agenda.profissional.Profissional;
import com.maestria.agenda.profissional.ProfissionalRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final ProfissionalRepository profissionalRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(ProfissionalRepository profissionalRepository, PasswordEncoder passwordEncoder) {
        this.profissionalRepository = profissionalRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 🔒 Apenas usuários autenticados podem acessar este endpoint
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<Object> getUserDetails(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(403).body("Usuário não autenticado.");
        }

        Optional<Profissional> profissionalOpt = Optional.ofNullable(profissionalRepository.findByLogin(userDetails.getUsername()));

        return profissionalOpt
                .<ResponseEntity<Object>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body("Usuário não encontrado."));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Profissional profissional) {
        if (profissionalRepository.existsByLogin(profissional.getLogin())) {
            return ResponseEntity.badRequest().body("Erro: Login já está em uso!");
        }

        // 🔒 Criptografar a senha antes de salvar
        profissional.setSenha(passwordEncoder.encode(profissional.getSenha()));

        // 💾 Salvar no banco
        profissionalRepository.save(profissional);

        return ResponseEntity.ok("✅ Usuário registrado com sucesso!");
    }
}
