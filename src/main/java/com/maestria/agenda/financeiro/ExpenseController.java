package com.maestria.agenda.financeiro;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
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
            LocalDate inicio = LocalDate.parse(startDate);
            LocalDate fim = LocalDate.parse(endDate);
            // Retorna apenas despesas pontuais, sem mesclagem
            List<ExpenseResponseDTO> despesasPontuais = expenseService.listarDespesas(inicio, fim, paidFilter);
            return ResponseEntity.ok(despesasPontuais);
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

    @GetMapping("/all-expenses")
    public ResponseEntity<?> listarTodasDespesas(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "all") String paidFilter) {
        try {
            LocalDate inicio = LocalDate.parse(startDate);
            LocalDate fim = LocalDate.parse(endDate);
            // Obter despesas pontuais (essas já terão o campo type definido como "REGULAR")
            List<ExpenseResponseDTO> despesasPontuais = expenseService.listarDespesas(inicio, fim, paidFilter);
            // Obter despesas fixas (essas já terão o campo type definido como "RECURRING")
            List<RecurringExpenseResponseDTO> despesasFixas = recurringExpenseService.listarDespesasFixasAtivas();
            // Combinar listas
            List<Object> todasDespesas = new ArrayList<>();
            todasDespesas.addAll(despesasPontuais);
            todasDespesas.addAll(despesasFixas);
            return ResponseEntity.ok(todasDespesas);
        } catch (Exception e) {
            logger.error("Erro ao listar todas as despesas: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao listar todas as despesas: " + e.getMessage());
        }
    }

    @PutMapping("/recurring-expenses/{id}/payment")
    public ResponseEntity<?> atualizarStatusPagamentoDespesaFixa(
            @PathVariable Long id,
            @RequestBody ExpensePaymentUpdateRequest request) {
        try {
            RecurringExpenseResponseDTO updatedExpense = recurringExpenseService.atualizarStatusPagamento(id, request.isPaid());
            return ResponseEntity.ok(updatedExpense);
        } catch (Exception e) {
            logger.error("Erro ao atualizar status de pagamento da despesa fixa: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao atualizar status de pagamento: " + e.getMessage());
        }
    }
    
    // NOVOS ENDPOINTS PARA TRATAMENTO DE INSTÂNCIAS DE DESPESAS FIXAS

    @PutMapping("/recurring-expenses/{recurringId}/instances/{instanceId}/payment")
    public ResponseEntity<?> atualizarStatusPagamentoInstanciaDespesaFixa(
            @PathVariable Long recurringId,
            @PathVariable Long instanceId,
            @RequestBody ExpensePaymentUpdateRequest request) {
        try {
            // Primeiro verificar se a instância existe
            boolean instanciaExiste = expenseService.verificarSeInstanciaExiste(instanceId, recurringId);
            if (!instanciaExiste) {
                return ResponseEntity.status(404).body("Instância de despesa fixa não encontrada.");
            }
            
            // Atualizar o status da instância como uma despesa normal
            ExpenseResponseDTO updatedInstance = expenseService.atualizarStatusPagamento(instanceId, request.isPaid());
            
            // Também atualizar o status da despesa fixa principal se necessário
            if (request.isUpdateMainRecurring() && recurringId != null) {
                recurringExpenseService.atualizarStatusPagamento(recurringId, request.isPaid());
            }
            
            return ResponseEntity.ok(updatedInstance);
        } catch (Exception e) {
            logger.error("Erro ao atualizar status de pagamento da instância: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao atualizar status de pagamento da instância: " + e.getMessage());
        }
    }
    
    @PutMapping("/recurring-expenses/{id}/reset-payment")
    public ResponseEntity<?> resetarStatusPagamentoDespesaFixa(@PathVariable Long id) {
        try {
            // Este endpoint é específico para reverter o status de pagamento para despesas problemáticas
            RecurringExpenseResponseDTO updatedExpense = recurringExpenseService.resetarStatusPagamento(id);
            return ResponseEntity.ok(updatedExpense);
        } catch (Exception e) {
            logger.error("Erro ao resetar status de pagamento da despesa fixa: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao resetar status de pagamento: " + e.getMessage());
        }
    }
    
    // Método especial para forçar exclusão ou correção de despesas problemáticas
    @PostMapping("/recurring-expenses/force-fix/{id}")
    public ResponseEntity<?> corrigirDespesaFixaProblematica(@PathVariable Long id) {
        try {
            boolean sucesso = recurringExpenseService.corrigirDespesaFixaProblematica(id);
            if (sucesso) {
                return ResponseEntity.ok("Despesa fixa corrigida com sucesso.");
            } else {
                return ResponseEntity.status(500).body("Não foi possível corrigir a despesa fixa.");
            }
        } catch (Exception e) {
            logger.error("Erro ao corrigir despesa fixa problemática: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao corrigir despesa fixa: " + e.getMessage());
        }
    }
}
