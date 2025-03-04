package com.maestria.agenda.controller;

import com.maestria.agenda.profissional.Profissional;
import com.maestria.agenda.profissional.ProfissionalRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*") // Permite acesso de todos os domínios
public class AuthController {

    private final ProfissionalRepository profissionalRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(ProfissionalRepository profissionalRepository, PasswordEncoder passwordEncoder) {
        this.profissionalRepository = profissionalRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/me")
    public ResponseEntity<Object> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(403).body("Usuário não autenticado.");
        }

        String username = userDetails.getUsername();
        Optional<Profissional> profissional = Optional.ofNullable(profissionalRepository.findByLogin(username));

        if (profissional.isPresent()) {
            Profissional user = profissional.get();
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("nome", user.getNome());
            response.put("login", user.getLogin());
            response.put("role", user.getRole());

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(404).body("Usuário não encontrado.");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid Profissional profissional) {
        if (profissionalRepository.existsByLogin(profissional.getLogin())) {
            return ResponseEntity.badRequest().body("Erro: Login já está em uso!");
        }

        if (profissional.getSenha().length() < 6) {
            return ResponseEntity.badRequest().body("Erro: A senha deve ter no mínimo 6 caracteres!");
        }

        // Criptografar a senha
        profissional.setSenha(passwordEncoder.encode(profissional.getSenha()));

        // Salvar o novo profissional
        profissionalRepository.save(profissional);

        return ResponseEntity.ok("Usuário registrado com sucesso!");
    }
}
