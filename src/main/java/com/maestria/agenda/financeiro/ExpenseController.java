package com.maestria.agenda.financeiro;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


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

    @PutMapping("/expenses/{id}")
    public ResponseEntity<?> editarDespesa(
            @PathVariable Long id,
            @RequestBody ExpenseRequestDTO requestDTO) {
        try {
            ExpenseResponseDTO updatedExpense = expenseService.updateExpense(id, requestDTO);
            return ResponseEntity.ok(updatedExpense);
        } catch (Exception e) {
            logger.error("Erro ao atualizar despesa: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao atualizar despesa: " + e.getMessage());
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

    @GetMapping("/recurring-expenses/{id}")
    public ResponseEntity<?> buscarDespesaFixa(@PathVariable Long id) {
        try {
            RecurringExpenseResponseDTO despesaFixa = recurringExpenseService.buscarDespesaFixa(id);
            return ResponseEntity.ok(despesaFixa);
        } catch (Exception e) {
            logger.error("Erro ao buscar despesa fixa: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao buscar despesa fixa: " + e.getMessage());
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

    @PutMapping("/recurring-expenses/{id}")
    public ResponseEntity<?> editarDespesaFixa(
            @PathVariable Long id,
            @RequestBody RecurringExpenseRequestDTO requestDTO) {
        try {
            RecurringExpenseResponseDTO updatedRecurringExpense = 
                recurringExpenseService.atualizarDespesaFixa(id, requestDTO);
            return ResponseEntity.ok(updatedRecurringExpense);
        } catch (Exception e) {
            logger.error("Erro ao atualizar despesa fixa: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao atualizar despesa fixa: " + e.getMessage());
        }
    }

    @PutMapping("/recurring-expenses/{id}/payment")
    public ResponseEntity<?> atualizarStatusPagamentoDespesaFixa(
            @PathVariable Long id,
            @RequestBody ExpensePaymentUpdateRequest request) {
        try {
            recurringExpenseService.atualizarStatusPagamento(id, request.isPaid());
            return ResponseEntity.ok("Status de pagamento atualizado com sucesso.");
        } catch (Exception e) {
            logger.error("Erro ao atualizar status de pagamento da despesa fixa: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao atualizar status de pagamento: " + e.getMessage());
        }
    }

    @DeleteMapping("/recurring-expenses/{id}")
    public ResponseEntity<?> deletarDespesaFixa(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean deleteInstances) {
        try {
            if (deleteInstances) {
                recurringExpenseService.excluirDespesaFixa(id);
            } else {
                recurringExpenseService.desativarDespesaFixa(id);
            }
            return ResponseEntity.ok("Despesa fixa excluída com sucesso.");
        } catch (Exception e) {
            logger.error("Erro ao excluir despesa fixa: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao excluir despesa fixa: " + e.getMessage());
        }
    }
    
    @PutMapping("/recurring-expenses/{recurringId}/instances/{instanceId}")
    public ResponseEntity<?> editarInstanciaDespesaFixa(
            @PathVariable Long recurringId,
            @PathVariable Long instanceId,
            @RequestBody ExpenseRequestDTO requestDTO) {
        try {
            // Verifica se a instância pertence à despesa fixa informada
            if (!expenseService.verificarSeInstanciaExiste(instanceId, recurringId)) {
                return ResponseEntity.status(404)
                    .body("Instância não encontrada ou não pertence à despesa fixa informada");
            }
            
            ExpenseResponseDTO updatedInstance = expenseService.updateExpense(instanceId, requestDTO);
            return ResponseEntity.ok(updatedInstance);
        } catch (Exception e) {
            logger.error("Erro ao atualizar instância de despesa fixa: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body("Erro ao atualizar instância de despesa fixa: " + e.getMessage());
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
    
    @GetMapping("/recurring-expenses/generate")
    public ResponseEntity<?> gerarInstanciasRecurringExpenses(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDate inicio = LocalDate.parse(startDate);
            LocalDate fim = LocalDate.parse(endDate);
            
            List<RecurringExpenseInstanceDTO> instancias = 
                recurringExpenseService.gerarInstanciasDespesasFixas(inicio, fim);
            
            return ResponseEntity.ok(instancias);
        } catch (Exception e) {
            logger.error("Erro ao gerar instâncias de despesas fixas: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body("Erro ao gerar instâncias de despesas fixas: " + e.getMessage());
        }
    }
}