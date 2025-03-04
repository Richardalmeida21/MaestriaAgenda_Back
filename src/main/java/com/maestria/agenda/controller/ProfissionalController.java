package com.maestria.agenda.controller;

import com.maestria.agenda.profissional.Profissional;
import com.maestria.agenda.profissional.ProfissionalRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/profissional")
@CrossOrigin(origins = "*")
public class ProfissionalController {

    private final ProfissionalRepository profissionalRepository;

    public ProfissionalController(ProfissionalRepository profissionalRepository) {
        this.profissionalRepository = profissionalRepository;
    }

    // Apenas ADMIN pode cadastrar profissionais
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> cadastrarProfissional(@Valid @RequestBody Profissional profissional) {
        if (profissionalRepository.findByLogin(profissional.getLogin()) != null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Erro: Login já cadastrado."));
        }

        if (profissional.getRole() == null) {
            profissional.setRole(Profissional.Role.PROFISSIONAL);  // Atribui ROLE_PROFISSIONAL se não especificado
        }

        if (profissional.getRole().equals("ROLE_ADMIN") && !temPermissaoParaCadastrarAdmin()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Erro: Somente administradores podem atribuir o role 'ROLE_ADMIN'."));
        }

        Profissional novoProfissional = profissionalRepository.save(profissional);
        return ResponseEntity.ok(novoProfissional);
    }

    // Apenas ADMIN pode listar todos os profissionais
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<Profissional> listarProfissionais() {
        return profissionalRepository.findAll();
    }

    // Apenas ADMIN pode excluir profissionais
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> deletarProfissional(@PathVariable Long id) {
        if (!profissionalRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        profissionalRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    public static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    private boolean temPermissaoParaCadastrarAdmin() {
        // Lógica para verificar se o usuário logado é um ADMIN
        return true;  // Adapte de acordo com sua implementação
    }
}
