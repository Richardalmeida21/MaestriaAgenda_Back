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

    /**
     * Endpoint para listar todas as comissões (apenas ADMIN)
     * Retorna comissões de todos os profissionais para o mês atual
     */
    @GetMapping("/comissoes")
public ResponseEntity<?> listarComissoes(
        @RequestParam(required = false) String dataInicio,
        @RequestParam(required = false) String dataFim,
        @AuthenticationPrincipal UserDetails userDetails) {
        
    logger.info("🔍 Solicitando listagem de comissões por {} no período de {} a {}", 
        userDetails.getUsername(), dataInicio, dataFim);

    // Verificar permissão ADMIN
    if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
        logger.warn("❌ Tentativa de acesso não autorizado às comissões por {}", userDetails.getUsername());
        return ResponseEntity.status(403).body("Acesso negado. Apenas ADMIN pode acessar todas as comissões.");
    }

    try {
        LocalDate inicio;
        LocalDate fim;
        
        // Use provided dates or default to current month
        if (dataInicio != null && dataFim != null) {
            inicio = LocalDate.parse(dataInicio);
            fim = LocalDate.parse(dataFim);
        } else {
            // Define o mês atual como período padrão
            inicio = LocalDate.now().withDayOfMonth(1);
            fim = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        }
        
        List<ComissaoResponseDTO> comissoes = comissaoService.listarTodasComissoesNoPeriodo(inicio, fim);
        return ResponseEntity.ok(comissoes);
    } catch (Exception e) {
        logger.error("❌ Erro ao listar comissões: {}", e.getMessage(), e);
        return ResponseEntity.status(500).body("Erro ao listar comissões: " + e.getMessage());
    }
}

    /**
     * Endpoint para calcular comissão de um profissional específico por período
     * Permite acesso pelo ADMIN ou pelo próprio profissional
     */
    @GetMapping("/comissoes/profissional/{id}")
    public ResponseEntity<?> calcularComissaoPorProfissional(
            @PathVariable Long id,
            @RequestParam String dataInicio,
            @RequestParam String dataFim,
            @AuthenticationPrincipal UserDetails userDetails) {

        logger.info("🔍 Solicitando comissão para profissional {} entre {} e {} por {}",
                id, dataInicio, dataFim, userDetails.getUsername());

        // Verificação de permissão
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
            logger.error("❌ Erro ao calcular comissão: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao calcular comissão: " + e.getMessage());
        }
    }
    
    /**
     * Endpoint para marcar uma comissão como paga ou não paga
     * Apenas administradores podem usar este endpoint
     */
    @PutMapping("/comissoes/profissional/{id}/paid")
    public ResponseEntity<?> marcarComissaoComoPaga(
            @PathVariable Long id,
            @RequestParam String dataInicio,
            @RequestParam String dataFim,
            @RequestParam boolean paid,
            @AuthenticationPrincipal UserDetails userDetails) {
            
        logger.info("🔄 Marcando comissão do profissional {} entre {} e {} como {} por {}",
                id, dataInicio, dataFim, paid ? "PAGA" : "NÃO PAGA", userDetails.getUsername());
                
        // Verificar se é ADMIN
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            logger.warn("❌ Tentativa não autorizada de atualizar status de pagamento por {}", 
                    userDetails.getUsername());
            return ResponseEntity.status(403).body("Acesso negado. Apenas administradores podem atualizar status de pagamento.");
        }
        
        try {
            LocalDate inicio = LocalDate.parse(dataInicio);
            LocalDate fim = LocalDate.parse(dataFim);
            
            // O serviço já fará todas as verificações de períodos sobrepostos e status
            ComissaoResponseDTO comissao = comissaoService.atualizarStatusPagamento(id, inicio, fim, paid);
            
            return ResponseEntity.ok(comissao);
        } catch (RuntimeException e) {
            logger.warn("⚠️ Erro de validação ao atualizar status de pagamento: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("❌ Erro ao atualizar status de pagamento da comissão: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao atualizar status de pagamento: " + e.getMessage());
        }
    }
}
