package com.maestria.agenda.controller;

import com.maestria.agenda.servico.CategoriaServico;
import com.maestria.agenda.servico.CategoriaServicoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/categorias")
@CrossOrigin(origins = "*")
public class CategoriaServicoController {

    private final CategoriaServicoRepository categoriaRepository;

    public CategoriaServicoController(CategoriaServicoRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping
    public ResponseEntity<List<CategoriaServico>> listarCategorias() {
        return ResponseEntity.ok(categoriaRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> criarCategoria(@RequestBody @Valid CategoriaServico categoria) {
        if (categoriaRepository.existsByNome(categoria.getNome())) {
            return ResponseEntity.badRequest().body("Já existe uma categoria com este nome.");
        }
        return ResponseEntity.ok(categoriaRepository.save(categoria));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> atualizarCategoria(@PathVariable Long id, @RequestBody @Valid CategoriaServico categoria) {
        return categoriaRepository.findById(id)
                .map(existing -> {
                    if (!existing.getNome().equals(categoria.getNome())
                            && categoriaRepository.existsByNome(categoria.getNome())) {
                        // throw new RuntimeException("Já existe uma categoria com este nome."); //
                        // Avoid throw in lambda if possible or catching outside
                        return ResponseEntity.badRequest().body("Já existe uma categoria com este nome.");
                    }
                    existing.setNome(categoria.getNome());
                    return ResponseEntity.ok(categoriaRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> deletarCategoria(@PathVariable Long id) {
        // TODO: Validar se existem serviços ou comissões usando esta categoria antes de
        // deletar?
        // Por enquanto, delete simples. DB constraint vai bloquear se houver FK.
        if (categoriaRepository.existsById(id)) {
            try {
                categoriaRepository.deleteById(id);
                return ResponseEntity.ok().build();
            } catch (Exception e) {
                return ResponseEntity.status(409).body("Não é possível excluir categoria em uso.");
            }
        }
        return ResponseEntity.notFound().build();
    }
}
