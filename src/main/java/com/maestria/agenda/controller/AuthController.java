package com.maestria.agenda.controller;

import com.maestria.agenda.profissional.Profissional;
import com.maestria.agenda.profissional.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*") // Permite requisições de qualquer origem
public class AuthController {

    @Autowired
    private ProfissionalRepository profissionalRepository;

    // 🔹 Retorna detalhes do usuário autenticado
    @GetMapping("/me")
public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
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
        response.put("role", user.getRole()); // ✅ Inclui a Role no retorno

        return ResponseEntity.ok(response);
    } else {
        return ResponseEntity.status(404).body("Usuário não encontrado.");
    }
}

}
