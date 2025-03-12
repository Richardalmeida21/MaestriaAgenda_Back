package com.maestria.agenda.controller;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
public class PasswordController {

    @GetMapping("/generate-password")
    public ResponseEntity<Object> generatePassword(@RequestParam String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            return ResponseEntity.badRequest().body("Erro: A senha não pode estar vazia.");
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encryptedPassword = encoder.encode(rawPassword);  // Criptografando a senha

        return ResponseEntity.ok().body(new PasswordResponse(encryptedPassword));
    }

    public static class PasswordResponse {
        private String senhaCriptografada;

        public PasswordResponse(String senhaCriptografada) {
            this.senhaCriptografada = senhaCriptografada;
        }

        public String getSenhaCriptografada() {
            return senhaCriptografada;
        }

        public void setSenhaCriptografada(String senhaCriptografada) {
            this.senhaCriptografada = senhaCriptografada;
        }
    }
}
