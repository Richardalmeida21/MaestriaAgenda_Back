package com.maestria.agenda.controller;

import com.maestria.agenda.profissional.Profissional;
import com.maestria.agenda.profissional.ProfissionalRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final ProfissionalRepository profissionalRepository;

    public AuthController(ProfissionalRepository profissionalRepository) {
        this.profissionalRepository = profissionalRepository;
    }

    @GetMapping("/user")
    public ResponseEntity<Object> getUserDetails(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(403).body("Usuário não autenticado.");
        }

        String username = userDetails.getUsername();
        Optional<Profissional> profissional = Optional.ofNullable(profissionalRepository.findByLogin(username));

        // ✅ Agora, ambos os retornos são ResponseEntity<Object>
        return profissional
                .<ResponseEntity<Object>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body("Usuário não encontrado."));
    }
}
