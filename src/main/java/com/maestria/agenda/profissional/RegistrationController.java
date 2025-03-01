package com.maestria.agenda.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.maestria.agenda.profissional.Profissional;
import com.maestria.agenda.profissional.ProfissionalRepository;
import com.maestria.agenda.profissional.RegistrationRequest;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class RegistrationController {

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistrationRequest registrationRequest) {
        if (profissionalRepository.findByLogin(registrationRequest.getUsername()) != null) {
            return ResponseEntity.status(400).body("Nome de usuário já existe.");
        }

        Profissional profissional = new Profissional();
        profissional.setLogin(registrationRequest.getUsername());
        profissional.setSenha(passwordEncoder.encode(registrationRequest.getSenha()));
        profissional.setNome(registrationRequest.getNome());
        profissionalRepository.save(profissional);

        return ResponseEntity.ok("Usuário registrado com sucesso!");
    }
}
