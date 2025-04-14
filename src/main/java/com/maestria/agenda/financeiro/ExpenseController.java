package com.maestria.agenda.financeiro;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/financeiro")
@CrossOrigin(origins = "*")
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
            LocalDate inicio = LocalDate.parse(startDate);
            LocalDate fim = LocalDate.parse(endDate);
            List<ExpenseResponseDTO> despesas = expenseService.listarDespesas(inicio, fim, paidFilter);
            return ResponseEntity.ok(despesas);
        } catch (Exception e) {
            logger.error("Erro ao listar despesas: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao listar despesas: " + e.getMessage());
        }
    }
    
    @PostMapping("/expenses")
    public ResponseEntity<?> criarDespesa(@RequestBody ExpenseRequestDTO requestDTO) {
        try {
            ExpenseResponseDTO createdExpense = expenseService.createExpense(requestDTO);
            return ResponseEntity.ok(createdExpense);
        } catch (Exception e) {
            logger.error("Erro ao criar despesa: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao criar despesa: " + e.getMessage());
        }
    }

    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<?> deletarDespesa(@PathVariable Long id) {
        try {
            expenseService.deleteExpense(id);
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
            ExpenseResponseDTO updatedExpense = expenseService.updatePaymentStatus(id, request.isPaid());
            return ResponseEntity.ok(updatedExpense);
        } catch (Exception e) {
            logger.error("Erro ao atualizar status de pagamento da despesa: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao atualizar status de pagamento: " + e.getMessage());
        }
    }

    @GetMapping("/recurring-expenses")
    public ResponseEntity<?> listarDespesasFixas() {
        try {
            List<RecurringExpenseResponseDTO> despesasFixas = recurringExpenseService.listarDespesasFixas();
            return ResponseEntity.ok(despesasFixas);
        } catch (Exception e) {
            logger.error("Erro ao listar despesas fixas: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao listar despesas fixas: " + e.getMessage());
        }
    }

    @PostMapping("/recurring-expenses")
    public ResponseEntity<?> criarDespesaFixa(@RequestBody RecurringExpenseRequestDTO requestDTO) {
        try {
            RecurringExpenseCreationResponse response = recurringExpenseService.criarDespesaFixa(requestDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erro ao criar despesa fixa: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao criar despesa fixa: " + e.getMessage());
        }
    }
    
    @GetMapping("/all-expenses")
    public ResponseEntity<?> listarTodasDespesas(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "all") String paidFilter) {
        try {
            LocalDate inicio = LocalDate.parse(startDate);
            LocalDate fim = LocalDate.parse(endDate);
            List<ExpenseResponseDTO> todasDespesas = expenseService.listarTodasDespesas(inicio, fim, paidFilter);
            return ResponseEntity.ok(todasDespesas);
        } catch (Exception e) {
            logger.error("Erro ao listar todas as despesas: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao listar todas as despesas: " + e.getMessage());
        }
    }
}