package com.maestria.agenda.controller;

import com.maestria.agenda.profissional.Profissional;
import com.maestria.agenda.profissional.ProfissionalRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/profissional")
@CrossOrigin(origins = "*")
public class ProfissionalController {

    private final ProfissionalRepository profissionalRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfissionalController(ProfissionalRepository profissionalRepository,
                                 PasswordEncoder passwordEncoder) {
        this.profissionalRepository = profissionalRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ✅ Rota pública para testar login de profissional
    @GetMapping("/teste-login/{login}")
    public ResponseEntity<String> testarLogin(@PathVariable String login) {
        Profissional profissional = profissionalRepository.findByLogin(login);
        if (profissional != null) {
            return ResponseEntity.ok("✅ Profissional encontrado: " + profissional.getId() + " - " + profissional.getNome());
        } else {
            return ResponseEntity.status(404).body("❌ Profissional não encontrado!");
        }
    }

    // ✅ ADMIN e PROFISSIONAIS podem visualizar todos os profissionais
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFISSIONAL')")
    public ResponseEntity<List<Profissional>> listarProfissionais() {
        return ResponseEntity.ok(profissionalRepository.findAll());
    }

    // ✅ Buscar profissional por ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFISSIONAL')")
    public ResponseEntity<?> buscarProfissionalPorId(@PathVariable Long id) {
        return profissionalRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.status(404).body("❌ Profissional não encontrado!"));
    }

    // ✅ Apenas ADMIN pode cadastrar profissionais
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Profissional> criarProfissional(@Valid @RequestBody Profissional profissional) {
        // Criptografa a senha antes de salvar
        if (profissional.getSenha() != null && !profissional.getSenha().isEmpty()) {
            profissional.setSenha(passwordEncoder.encode(profissional.getSenha()));
        }
        
        Profissional novoProfissional = profissionalRepository.save(profissional);
        return ResponseEntity.ok(novoProfissional);
    }

    // ✅ Apenas ADMIN pode atualizar profissionais
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> atualizarProfissional(@PathVariable Long id, @Valid @RequestBody Profissional profissionalAtualizado) {
        return profissionalRepository.findById(id)
            .map(profissionalExistente -> {
                // Atualizar dados do profissional (apenas campos que existem no modelo)
                profissionalExistente.setNome(profissionalAtualizado.getNome());
                profissionalExistente.setLogin(profissionalAtualizado.getLogin());
                
                // Se houver uma nova senha e ela não estiver vazia, criptografe-a
                if (profissionalAtualizado.getSenha() != null && !profissionalAtualizado.getSenha().isEmpty()) {
                    String senhaCriptografada = passwordEncoder.encode(profissionalAtualizado.getSenha());
                    profissionalExistente.setSenha(senhaCriptografada);
                }
                
                // Atualizar o role se fornecido
                if (profissionalAtualizado.getRole() != null) {
                    profissionalExistente.setRole(profissionalAtualizado.getRole());
                }
                
                // Salvar as alterações
                Profissional updated = profissionalRepository.save(profissionalExistente);
                return ResponseEntity.ok(updated);
            })
            .orElse(ResponseEntity.status(404).body("❌ Profissional não encontrado!"));
    }

    // ✅ Apenas ADMIN pode excluir profissionais
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> deletarProfissional(@PathVariable Long id) {
        if (profissionalRepository.existsById(id)) {
            profissionalRepository.deleteById(id);
            return ResponseEntity.ok("✅ Profissional deletado!");
        }
        return ResponseEntity.status(404).body("❌ Profissional não encontrado!");
    }
}
