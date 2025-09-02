package com.maestria.agenda.financeiro;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/taxas-pagamento")
@CrossOrigin(origins = "*")
public class TaxaPagamentoController {

    private static final Logger logger = LoggerFactory.getLogger(TaxaPagamentoController.class);
    
    private final TaxaPagamentoService taxaPagamentoService;

    public TaxaPagamentoController(TaxaPagamentoService taxaPagamentoService) {
        this.taxaPagamentoService = taxaPagamentoService;
    }

    /**
     * Lista todas as configurações de taxa
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> listarTaxas(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔍 Solicitando lista de taxas por: {}", userDetails.getUsername());
        
        try {
            List<TaxaPagamento> taxas = taxaPagamentoService.listarTodasTaxas();
            return ResponseEntity.ok(taxas);
        } catch (Exception e) {
            logger.error("❌ Erro ao listar taxas", e);
            return ResponseEntity.status(500).body("Erro ao listar taxas.");
        }
    }

    /**
     * Lista todos os tipos de pagamento disponíveis
     */
    @GetMapping("/tipos-disponiveis")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<TiposPagamentoResponse>> obterTiposDisponiveis(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔍 Obtendo tipos de pagamento disponíveis por: {}", userDetails.getUsername());
        
        try {
            List<TiposPagamentoResponse> tipos = Arrays.stream(PagamentoTipo.values())
                .map(tipo -> new TiposPagamentoResponse(tipo.name(), tipo.getDescricao()))
                .collect(Collectors.toList());
            
            logger.info("✅ Retornando {} tipos de pagamento", tipos.size());
            return ResponseEntity.ok(tipos);
        } catch (Exception e) {
            logger.error("❌ Erro ao obter tipos de pagamento", e);
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Obtém a taxa de um tipo de pagamento específico
     */
    @GetMapping("/{tipoPagamento}")
    public ResponseEntity<?> obterTaxa(@PathVariable String tipoPagamento) {
        logger.info("🔍 Consultando taxa para: {}", tipoPagamento);
        
        try {
            PagamentoTipo tipo = PagamentoTipo.fromString(tipoPagamento);
            if (tipo == null) {
                return ResponseEntity.badRequest().body("Tipo de pagamento inválido.");
            }
            
            double taxa = taxaPagamentoService.obterTaxa(tipo);
            return ResponseEntity.ok(new TaxaResponse(tipo, taxa));
        } catch (Exception e) {
            logger.error("❌ Erro ao obter taxa", e);
            return ResponseEntity.status(500).body("Erro ao obter taxa.");
        }
    }

    /**
     * Configura ou atualiza a taxa de um tipo de pagamento
     */
    @PostMapping("/configurar")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> configurarTaxa(
            @RequestBody @Valid TaxaRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        logger.info("🔍 Configurando taxa {} = {}% por: {}", 
                request.tipoPagamento(), request.taxa(), userDetails.getUsername());
        
        try {
            PagamentoTipo tipo = PagamentoTipo.fromString(request.tipoPagamento());
            if (tipo == null) {
                return ResponseEntity.badRequest().body("Tipo de pagamento inválido.");
            }
            
            TaxaPagamento taxaConfig = taxaPagamentoService.configurarTaxa(tipo, request.taxa());
            
            logger.info("✅ Taxa configurada com sucesso: {} = {}%", tipo, request.taxa());
            return ResponseEntity.ok(taxaConfig);
        } catch (Exception e) {
            logger.error("❌ Erro ao configurar taxa", e);
            return ResponseEntity.status(500).body("Erro ao configurar taxa.");
        }
    }

    /**
     * Desativa uma configuração de taxa (volta para padrão)
     */
    @DeleteMapping("/{tipoPagamento}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> desativarTaxa(
            @PathVariable String tipoPagamento,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        logger.info("🔍 Desativando taxa para {} por: {}", tipoPagamento, userDetails.getUsername());
        
        try {
            PagamentoTipo tipo = PagamentoTipo.fromString(tipoPagamento);
            if (tipo == null) {
                return ResponseEntity.badRequest().body("Tipo de pagamento inválido.");
            }
            
            taxaPagamentoService.desativarTaxa(tipo);
            
            logger.info("✅ Taxa desativada com sucesso para: {}", tipo);
            return ResponseEntity.ok("Taxa desativada. Voltará para valor padrão.");
        } catch (Exception e) {
            logger.error("❌ Erro ao desativar taxa", e);
            return ResponseEntity.status(500).body("Erro ao desativar taxa.");
        }
    }

    /**
     * Inicializa todas as taxas padrão no banco
     */
    @PostMapping("/inicializar")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> inicializarTaxasPadrao(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("🔍 Inicializando taxas padrão por: {}", userDetails.getUsername());
        
        try {
            taxaPagamentoService.inicializarTaxasPadrao();
            
            logger.info("✅ Taxas padrão inicializadas com sucesso");
            return ResponseEntity.ok("Taxas padrão inicializadas com sucesso.");
        } catch (Exception e) {
            logger.error("❌ Erro ao inicializar taxas padrão", e);
            return ResponseEntity.status(500).body("Erro ao inicializar taxas padrão.");
        }
    }

    // DTOs internos
    public record TaxaRequest(String tipoPagamento, Double taxa) {}
    
    public record TaxaResponse(PagamentoTipo tipoPagamento, Double taxa) {}
    
    public record TiposPagamentoResponse(String codigo, String descricao) {}
}
