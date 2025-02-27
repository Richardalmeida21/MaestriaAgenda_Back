package com.maestria.agenda.profissional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegistrationController {

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistrationRequest registrationRequest) {
        if (profissionalRepository.findByLogin(registrationRequest.getUsername()) != null) {
            return ResponseEntity.status(400).body("Username already exists");
        }

        Profissional profissional = new Profissional();
        profissional.setLogin(registrationRequest.getUsername());
        profissional.setSenha(passwordEncoder.encode(registrationRequest.getPassword()));
        profissionalRepository.save(profissional);

        return ResponseEntity.ok("User registered successfully");
    }
}