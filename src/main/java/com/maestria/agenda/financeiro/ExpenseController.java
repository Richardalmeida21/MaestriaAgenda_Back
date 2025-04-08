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

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
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
}
