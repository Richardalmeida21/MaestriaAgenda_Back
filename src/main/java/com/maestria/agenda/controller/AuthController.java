package com.maestria.agenda.controller;

import com.maestria.agenda.profissional.Profissional;
import com.maestria.agenda.profissional.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final ProfissionalRepository profissionalRepository;

    @Autowired
    public AuthController(ProfissionalRepository profissionalRepository) {
        this.profissionalRepository = profissionalRepository;
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
    public ResponseEntity<?> registerUser(@RequestBody Profissional profissional) {
        if (profissionalRepository.existsByLogin(profissional.getLogin())) {
            return ResponseEntity.badRequest().body("Erro: Login já está em uso!");
        }

        // 🔹 Se não for passado um role, definir como "PROFISSIONAL"
        if (profissional.getRole() == null || profissional.getRole().isEmpty()) {
            profissional.setRole("PROFISSIONAL");
        }

        // 🔹 Salvar o novo profissional
        profissionalRepository.save(profissional);

        return ResponseEntity.ok(Map.of("message", "Usuário registrado com sucesso!", "login", profissional.getLogin()));
    }
}
