package com.maestria.agenda.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @GetMapping("/user")
    public ResponseEntity<?> getUserDetails() {
        // Obtém o usuário autenticado no contexto de segurança do Spring
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            return ResponseEntity.ok(userDetails);
        } else {
            return ResponseEntity.status(403).body("Usuário não autenticado.");
        }
    }
}
