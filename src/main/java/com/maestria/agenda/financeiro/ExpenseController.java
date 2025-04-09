package com.maestria.agenda.financeiro;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/financeiro")
public class ExpenseController {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseController.class);
    private final ExpenseService expenseService;
    private final RecurringExpenseService recurringExpenseService;

    public ExpenseController(ExpenseService expenseService, RecurringExpenseService recurringExpenseService) {
        this.expenseService = expenseService;
        this.recurringExpenseService = recurringExpenseService;
    }

    @GetMapping("/expenses")
    public ResponseEntity<?> listarExpenses(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "all") String paidFilter) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            List<ExpenseResponseDTO> expenses = expenseService.listarDespesas(start, end, paidFilter);
            return ResponseEntity.ok(expenses);
        } catch (Exception e) {
            logger.error("Erro ao listar despesas: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao listar despesas: " + e.getMessage());
        }
    }
    
    @PostMapping("/expenses")
    public ResponseEntity<?> criarDespesa(@RequestBody ExpenseRequestDTO requestDTO) {
        try {
            ExpenseResponseDTO createdExpense = expenseService.criarDespesa(requestDTO);
            return ResponseEntity.ok(createdExpense);
        } catch (Exception e) {
            logger.error("Erro ao criar despesa: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao criar despesa: " + e.getMessage());
        }
    }

    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<?> deletarDespesa(@PathVariable Long id) {
        try {
            expenseService.deletarDespesa(id);
            return ResponseEntity.ok("Despesa deletada com sucesso.");
        } catch (Exception e) {
            logger.error("Erro ao deletar despesa: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao deletar despesa: " + e.getMessage());
        }
    }
    
    @PutMapping("/expenses/{id}/payment")
    public ResponseEntity<?> atualizarStatusPagamento(
            @PathVariable Long id,
            @RequestBody ExpensePaymentUpdateRequest request) {
        try {
            ExpenseResponseDTO updatedExpense = expenseService.atualizarStatusPagamento(id, request.isPaid());
            return ResponseEntity.ok(updatedExpense);
        } catch (Exception e) {
            logger.error("Erro ao atualizar status de pagamento da despesa: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao atualizar status de pagamento: " + e.getMessage());
        }
    }

    // ===== ENDPOINTS PARA DESPESAS FIXAS =====

    @GetMapping("/recurring-expenses")
    public ResponseEntity<?> listarDespesasFixas() {
        try {
            List<RecurringExpenseResponseDTO> despesasFixas = recurringExpenseService.listarDespesasFixasAtivas();
            return ResponseEntity.ok(despesasFixas);
        } catch (Exception e) {
            logger.error("Erro ao listar despesas fixas: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao listar despesas fixas: " + e.getMessage());
        }
    }

    @GetMapping("/recurring-expenses/{id}")
    public ResponseEntity<?> buscarDespesaFixa(@PathVariable Long id) {
        try {
            RecurringExpenseResponseDTO despesaFixa = recurringExpenseService.buscarDespesaFixa(id);
            return ResponseEntity.ok(despesaFixa);
        } catch (Exception e) {
            logger.error("Erro ao buscar despesa fixa: {}", e.getMessage(), e);
            return ResponseEntity.status(404).body("Erro ao buscar despesa fixa: " + e.getMessage());
        }
    }

    @PostMapping("/recurring-expenses")
    public ResponseEntity<?> criarDespesaFixa(@RequestBody RecurringExpenseRequestDTO requestDTO) {
        try {
            RecurringExpenseResponseDTO despesaCriada = recurringExpenseService.criarDespesaFixa(requestDTO);
            return ResponseEntity.ok(despesaCriada);
        } catch (Exception e) {
            logger.error("Erro ao criar despesa fixa: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao criar despesa fixa: " + e.getMessage());
        }
    }

    @PutMapping("/recurring-expenses/{id}")
    public ResponseEntity<?> atualizarDespesaFixa(
            @PathVariable Long id,
            @RequestBody RecurringExpenseRequestDTO requestDTO) {
        try {
            RecurringExpenseResponseDTO despesaAtualizada = recurringExpenseService.atualizarDespesaFixa(id, requestDTO);
            return ResponseEntity.ok(despesaAtualizada);
        } catch (Exception e) {
            logger.error("Erro ao atualizar despesa fixa: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao atualizar despesa fixa: " + e.getMessage());
        }
    }

    @DeleteMapping("/recurring-expenses/{id}")
    public ResponseEntity<?> excluirDespesaFixa(@PathVariable Long id) {
        try {
            recurringExpenseService.excluirDespesaFixa(id);
            return ResponseEntity.ok("Despesa fixa excluída com sucesso.");
        } catch (Exception e) {
            logger.error("Erro ao excluir despesa fixa: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao excluir despesa fixa: " + e.getMessage());
        }
    }

    @PutMapping("/recurring-expenses/{id}/deactivate")
    public ResponseEntity<?> desativarDespesaFixa(@PathVariable Long id) {
        try {
            recurringExpenseService.desativarDespesaFixa(id);
            return ResponseEntity.ok("Despesa fixa desativada com sucesso.");
        } catch (Exception e) {
            logger.error("Erro ao desativar despesa fixa: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao desativar despesa fixa: " + e.getMessage());
        }
    }

    @PostMapping("/recurring-expenses/generate")
    public ResponseEntity<?> gerarDespesasParaPeriodo(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDate inicio = LocalDate.parse(startDate);
            LocalDate fim = LocalDate.parse(endDate);
            
            List<ExpenseResponseDTO> despesasGeradas = recurringExpenseService.gerarDespesasParaPeriodo(inicio, fim);
            return ResponseEntity.ok(despesasGeradas);
        } catch (Exception e) {
            logger.error("Erro ao gerar despesas para o período: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao gerar despesas para o período: " + e.getMessage());
        }
    }
}
