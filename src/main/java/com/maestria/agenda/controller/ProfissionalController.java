package com.maestria.agenda.controller;

import com.maestria.agenda.profissional.Profissional;
import com.maestria.agenda.profissional.ProfissionalRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/profissional")
@CrossOrigin(origins = "*")
public class ProfissionalController {

    private final ProfissionalRepository profissionalRepository;
    private final PasswordEncoder passwordEncoder;  // Injeção do PasswordEncoder

    
    public ProfissionalController(ProfissionalRepository profissionalRepository, PasswordEncoder passwordEncoder) {
        this.profissionalRepository = profissionalRepository;
        this.passwordEncoder = passwordEncoder;  // Inicializa o PasswordEncoder
    }

    @PostMapping
    public Profissional cadastrarProfissional(@RequestBody Profissional profissional) {
        // Gerar login único antes de salvar o profissional
        String generatedLogin = "defaultLogin";  // Pode ser qualquer valor padrão
        profissional.setLogin(generateUniqueLogin(generatedLogin));

        // Criptografar a senha antes de salvar no banco
        String senhaCriptografada = passwordEncoder.encode(profissional.getSenha());
        profissional.setSenha(senhaCriptografada);

        // O JPA gerará o ID automaticamente, não é necessário passar o 'id' na requisição
        return profissionalRepository.save(profissional);
    }

    @GetMapping
    public List<Profissional> listarProfissionais() {
        return profissionalRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Profissional> buscarProfissionalPorId(@PathVariable Long id) {
        Optional<Profissional> profissional = profissionalRepository.findById(id);
        return profissional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarProfissional(@PathVariable Long id) {
        if (profissionalRepository.existsById(id)) {
            profissionalRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Método para gerar um login único
    private String generateUniqueLogin(String baseLogin) {
        String newLogin = baseLogin;
        int counter = 1;

        // Verificando se o login já existe no banco de dados
        while (profissionalRepository.existsByLogin(newLogin)) {
            newLogin = baseLogin + counter;
            counter++;
        }

        return newLogin;
    }
}
