package com.maestria.agenda.financeiro;

import com.maestria.agenda.profissional.Profissional;
import com.maestria.agenda.profissional.ProfissionalRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/financeiro")
@CrossOrigin(origins = "*")
public class ComissaoController {

    private static final Logger logger = LoggerFactory.getLogger(ComissaoController.class);

    private final ComissaoService comissaoService;
    private final ProfissionalRepository profissionalRepository;

    public ComissaoController(ComissaoService comissaoService, ProfissionalRepository profissionalRepository) {
        this.comissaoService = comissaoService;
        this.profissionalRepository = profissionalRepository;
    }

    // Lista todas as comissões (apenas ADMIN)
    @GetMapping("/comissoes")
    public ResponseEntity<?> listarComissoes(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔍 Solicitando cálculo de comissões por {}", userDetails.getUsername());

        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            logger.warn("❌ Tentativa de acesso às comissões sem permissão por {}", userDetails.getUsername());
            return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode acessar as comissões.");
        }

        try {
            List<Object[]> comissoes = comissaoService.listarComissoes();
            return ResponseEntity.ok(comissoes);
        } catch (Exception e) {
            logger.error("❌ Erro ao calcular comissões", e);
            return ResponseEntity.status(500).body("Erro ao calcular comissões: " + e.getMessage());
        }
    }

    // Calcula comissão de um profissional específico por período
    @GetMapping("/comissoes/profissional/{id}")
    public ResponseEntity<?> calcularComissaoPorProfissional(
            @PathVariable Long id,
            @RequestParam String dataInicio,
            @RequestParam String dataFim,
            @AuthenticationPrincipal UserDetails userDetails) {

        logger.info("🔍 Solicitando comissão para profissional {} entre {} e {} por {}",
                id, dataInicio, dataFim, userDetails.getUsername());

        // Verificar se o usuário é ADMIN ou o próprio profissional
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));
        boolean isProfissionalAcessandoPropriosDados = false;

        if (!isAdmin) {
            // Verificar se é o próprio profissional acessando seus dados
            Profissional profissional = profissionalRepository.findByLogin(userDetails.getUsername());
            if (profissional != null && profissional.getId() == id) {
                isProfissionalAcessandoPropriosDados = true;
            }
        }

        if (!isAdmin && !isProfissionalAcessandoPropriosDados) {
            logger.warn("❌ Acesso negado para visualizar comissões do profissional {}", id);
            return ResponseEntity.status(403).body("Acesso negado. Você só pode ver suas próprias comissões.");
        }

        try {
            LocalDate inicio = LocalDate.parse(dataInicio);
            LocalDate fim = LocalDate.parse(dataFim);

            ComissaoResponseDTO comissao = comissaoService.calcularComissaoPorPeriodo(id, inicio, fim);
            return ResponseEntity.ok(comissao);
        } catch (Exception e) {
            logger.error("❌ Erro ao calcular comissão", e);
            return ResponseEntity.status(500).body("Erro ao calcular comissão: " + e.getMessage());
        }
    }

    // Endpoint para profissional ver suas próprias comissões
    // Altere esta linha:
    @GetMapping("/comissoes/minhas")
    public ResponseEntity<?> consultarMinhasComissoes(
            @RequestParam String dataInicio,
            @RequestParam String dataFim,
            @AuthenticationPrincipal UserDetails userDetails) {

        logger.info("🔍 {} solicitando suas comissões entre {} e {}",
                userDetails.getUsername(), dataInicio, dataFim);

        try {
            // Buscar o profissional
            Profissional profissional = profissionalRepository.findByLogin(userDetails.getUsername());
            if (profissional == null) {
                logger.warn("❌ Profissional não encontrado: {}", userDetails.getUsername());
                return ResponseEntity.status(403).body("Profissional não encontrado.");
            }

            LocalDate inicio = LocalDate.parse(dataInicio);
            LocalDate fim = LocalDate.parse(dataFim);

            // CORREÇÃO: Usar ComissaoResponseDTO em vez de
            // ComissaoService.ComissaoResponseDTO
            ComissaoResponseDTO comissao = comissaoService.calcularComissaoPorPeriodo(profissional.getId(), inicio,
                    fim);
            return ResponseEntity.ok(comissao);
        } catch (Exception e) {
            logger.error("❌ Erro ao calcular comissões", e);
            return ResponseEntity.status(500).body("Erro ao calcular comissões: " + e.getMessage());
        }
    }
}