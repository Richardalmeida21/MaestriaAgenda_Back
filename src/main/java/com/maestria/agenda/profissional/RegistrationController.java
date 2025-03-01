package com.maestria.agenda.profissional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth") // 🔥 Certifique-se que a rota está correta no SecurityConfig
@CrossOrigin(origins = "*")
public class RegistrationController {

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistrationRequest registrationRequest) {
        // 🔍 Validação de campos obrigatórios
        if (registrationRequest.getUsername() == null || registrationRequest.getUsername().isEmpty()) {
            return ResponseEntity.badRequest().body("Erro: Nome de usuário não pode ser vazio.");
        }
        if (registrationRequest.getSenha() == null || registrationRequest.getSenha().isEmpty()) {
            return ResponseEntity.badRequest().body("Erro: Senha não pode ser vazia.");
        }
        if (registrationRequest.getNome() == null || registrationRequest.getNome().isEmpty()) {
            return ResponseEntity.badRequest().body("Erro: Nome não pode ser vazio.");
        }

        // 🔍 Verifica se o login já existe no banco
        if (profissionalRepository.findByLogin(registrationRequest.getUsername()) != null) {
            return ResponseEntity.status(400).body("Erro: Nome de usuário já existe. Escolha outro.");
        }

        // 🔒 Criptografa a senha antes de salvar
        Profissional profissional = new Profissional();
        profissional.setLogin(registrationRequest.getUsername());
        profissional.setSenha(passwordEncoder.encode(registrationRequest.getSenha())); // Criptografa senha
        profissional.setNome(registrationRequest.getNome());
        profissionalRepository.save(profissional);

        return ResponseEntity.ok("Usuário registrado com sucesso!");
    }
}
