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
        // Verifica se o login já está em uso
        if (profissionalRepository.findByLogin(profissional.getLogin()) != null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Erro: Login já cadastrado."));
        }

        // Verifica se o campo 'role' não está vazio. Se estiver vazio, atribui 'ROLE_PROFISSIONAL' por padrão.
        if (profissional.getRole() == null) {
            profissional.setRole(Profissional.Role.PROFISSIONAL);  // Atribuindo o valor padrão como PROFISSIONAL
        }
        

        // Caso o role seja "ROLE_ADMIN" e o usuário não seja ADMIN, retorna erro.
        if (profissional.getRole().equals("ROLE_ADMIN") && !temPermissaoParaCadastrarAdmin()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Erro: Somente administradores podem atribuir o role 'ROLE_ADMIN'."));
        }

        // Salva o profissional
        Profissional novoProfissional = profissionalRepository.save(profissional);
        return ResponseEntity.ok(novoProfissional);
    }

    // Apenas ADMIN pode listar todos os profissionais
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<Profissional> listarProfissionais() {
        return profissionalRepository.findAll();
    }

    // Classe para resposta de erro
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

    // Verifica se o usuário logado tem permissão para atribuir o role 'ROLE_ADMIN'
    private boolean temPermissaoParaCadastrarAdmin() {
        // Lógica para verificar se o usuário logado é um ADMIN
        // Isso pode ser feito com a verificação do principal ou das authorities do usuário logado
        // Exemplo de verificação baseada nas authorities
        // Você pode adaptar isso de acordo com a sua implementação de segurança.
        return true;  // Aqui você pode implementar a lógica de verificação de permissões
    }
}
