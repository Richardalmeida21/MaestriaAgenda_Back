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
import java.util.Map;

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
     * Retorna comissões de todos os profissionais para o período especificado
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
            
            List<Profissional> profissionais = profissionalRepository.findAll();
            List<ComissaoResponseDTO> comissoes = profissionais.stream()
                .map(prof -> comissaoService.calcularComissaoPorPeriodo(prof.getId(), inicio, fim))
                .toList();
                
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
     * Endpoint para registrar um pagamento de comissão
     * Apenas administradores podem usar este endpoint
     */
    @PostMapping("/comissoes/profissional/{id}/pagamento")
    public ResponseEntity<?> registrarPagamentoComissao(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload,
            @AuthenticationPrincipal UserDetails userDetails) {
            
        logger.info("💰 Registrando pagamento de comissão para profissional {} no valor de {} em {} por {}",
                id, payload.get("valorPago"), payload.get("dataPagamento"), userDetails.getUsername());
                
        // Verificar se é ADMIN
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            logger.warn("❌ Tentativa não autorizada de registrar pagamento por {}", 
                    userDetails.getUsername());
            return ResponseEntity.status(403).body("Acesso negado. Apenas administradores podem registrar pagamentos.");
        }
        
        try {
            String dataPagamentoStr = (String) payload.get("dataPagamento");
            Double valorPago = ((Number) payload.get("valorPago")).doubleValue();
            String observacao = (String) payload.get("observacao");
            
            LocalDate data = LocalDate.parse(dataPagamentoStr);
            
            ComissaoResponseDTO comissao = comissaoService.registrarPagamentoComissao(id, data, valorPago, observacao);
            
            return ResponseEntity.ok(comissao);
        } catch (Exception e) {
            logger.error("❌ Erro ao registrar pagamento de comissão: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao registrar pagamento: " + e.getMessage());
        }
    }
    
    /**
     * Endpoint para listar os pagamentos de comissão de um profissional em um período
     * Permite acesso pelo ADMIN ou pelo próprio profissional
     */
    @GetMapping("/comissoes/profissional/{id}/pagamentos")
    public ResponseEntity<?> listarPagamentosComissao(
            @PathVariable Long id,
            @RequestParam String dataInicio,
            @RequestParam String dataFim,
            @AuthenticationPrincipal UserDetails userDetails) {
            
        logger.info("🔍 Solicitando pagamentos de comissão do profissional {} entre {} e {} por {}",
                id, dataInicio, dataFim, userDetails.getUsername());
                
        // Verificação de permissão
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));
        boolean isProfissionalAcessandoPropriosDados = false;

        if (!isAdmin) {
            Profissional profissional = profissionalRepository.findByLogin(userDetails.getUsername());
            if (profissional != null && profissional.getId() == id) {
                isProfissionalAcessandoPropriosDados = true;
            }
        }

        if (!isAdmin && !isProfissionalAcessandoPropriosDados) {
            logger.warn("❌ Acesso negado para visualizar pagamentos do profissional {}", id);
            return ResponseEntity.status(403).body("Acesso negado. Você só pode ver seus próprios pagamentos.");
        }
        
        try {
            LocalDate inicio = LocalDate.parse(dataInicio);
            LocalDate fim = LocalDate.parse(dataFim);
            
            List<ComissaoPagamento> pagamentos = comissaoService.listarPagamentosPorPeriodo(id, inicio, fim);
            
            return ResponseEntity.ok(pagamentos);
        } catch (Exception e) {
            logger.error("❌ Erro ao listar pagamentos: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao listar pagamentos: " + e.getMessage());
        }
    }
}
