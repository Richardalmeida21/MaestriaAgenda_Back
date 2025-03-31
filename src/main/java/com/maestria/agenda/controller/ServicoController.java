package com.maestria.agenda.controller;

import com.maestria.agenda.servico.DadosServico;
import com.maestria.agenda.servico.Servico;
import com.maestria.agenda.servico.ServicoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/servico")
@CrossOrigin(origins = "*")
public class ServicoController {

    private static final Logger logger = LoggerFactory.getLogger(ServicoController.class);
    
    private final ServicoRepository servicoRepository;

    public ServicoController(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    @GetMapping
    public ResponseEntity<List<Servico>> listarServicos() {
        logger.info("🔍 Listando todos os serviços");
        return ResponseEntity.ok(servicoRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarServicoPorId(@PathVariable Long id) {
        logger.info("🔍 Buscando serviço com ID: {}", id);
        return servicoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.warn("❌ Serviço não encontrado. ID: {}", id);
                    return ResponseEntity.status(404).build();
                });
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> cadastrarServico(
            @RequestBody @Valid DadosServico dados,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        logger.info("🔍 Solicitação para cadastrar serviço por: {}", userDetails.getUsername());
        
        try {
            if (servicoRepository.existsByNome(dados.nome())) {
                logger.warn("❌ Já existe um serviço com o nome: {}", dados.nome());
                return ResponseEntity.badRequest().body("Já existe um serviço com este nome.");
            }
            
            Servico servico = new Servico();
            servico.setNome(dados.nome());
            servico.setValor(dados.valor());
            servico.setDescricao(dados.descricao());
            servico.setDuracao(dados.duracao());
            
            servicoRepository.save(servico);
            
            logger.info("✅ Serviço cadastrado com sucesso: {}", servico);
            return ResponseEntity.ok(servico);
        } catch (Exception e) {
            logger.error("❌ Erro ao cadastrar serviço", e);
            return ResponseEntity.status(500).body("Erro ao cadastrar serviço.");
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> atualizarServico(
            @PathVariable Long id,
            @RequestBody @Valid DadosServico dados,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        logger.info("🔍 Solicitação para atualizar serviço {} por: {}", id, userDetails.getUsername());
        
        try {
            return servicoRepository.findById(id)
                    .map(servico -> {
                        // Verifica se já existe outro serviço com este nome
                        Servico existente = servicoRepository.findByNome(dados.nome());
                        if (existente != null && !existente.getId().equals(id)) {
                            logger.warn("❌ Já existe outro serviço com o nome: {}", dados.nome());
                            return ResponseEntity.badRequest().body("Já existe outro serviço com este nome.");
                        }
                        
                        servico.setNome(dados.nome());
                        servico.setValor(dados.valor());
                        servico.setDescricao(dados.descricao());
                        servico.setDuracao(dados.duracao());
                        
                        servicoRepository.save(servico);
                        
                        logger.info("✅ Serviço atualizado com sucesso: {}", servico);
                        return ResponseEntity.ok(servico);
                    })
                    .orElseGet(() -> {
                        logger.warn("❌ Serviço não encontrado. ID: {}", id);
                        return ResponseEntity.status(404).body("Serviço não encontrado.");
                    });
        } catch (Exception e) {
            logger.error("❌ Erro ao atualizar serviço", e);
            return ResponseEntity.status(500).body("Erro ao atualizar serviço.");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> excluirServico(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        logger.info("🔍 Solicitação para excluir serviço {} por: {}", id, userDetails.getUsername());
        
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            logger.warn("❌ Tentativa de exclusão de serviço sem permissão por {}", userDetails.getUsername());
            return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode excluir serviços.");
        }
        
        try {
            if (!servicoRepository.existsById(id)) {
                logger.warn("❌ Serviço não encontrado. ID: {}", id);
                return ResponseEntity.status(404).body("Serviço não encontrado.");
            }
            
            servicoRepository.deleteById(id);
            
            logger.info("✅ Serviço excluído com sucesso. ID: {}", id);
            return ResponseEntity.ok("Serviço excluído com sucesso.");
        } catch (Exception e) {
            logger.error("❌ Erro ao excluir serviço", e);
            return ResponseEntity.status(500).body("Erro ao excluir serviço.");
        }
    }
}