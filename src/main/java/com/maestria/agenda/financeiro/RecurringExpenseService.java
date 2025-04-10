package com.maestria.agenda.financeiro;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecurringExpenseService {
    
    private final RecurringExpenseRepository recurringExpenseRepository;
    private final ExpenseRepository expenseRepository;
    private final Logger logger = LoggerFactory.getLogger(RecurringExpenseService.class);
    
    public RecurringExpenseService(RecurringExpenseRepository recurringExpenseRepository,
                                  ExpenseRepository expenseRepository) {
        this.recurringExpenseRepository = recurringExpenseRepository;
        this.expenseRepository = expenseRepository;
    }
    
    /**
     * Lista todas as despesas fixas
     */
    public List<RecurringExpenseResponseDTO> listarDespesasFixas() {
        return recurringExpenseRepository.findAll().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Lista despesas fixas ativas
     */
    public List<RecurringExpenseResponseDTO> listarDespesasFixasAtivas() {
        return recurringExpenseRepository.findByActiveTrue().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Busca uma despesa fixa por ID
     */
    public RecurringExpenseResponseDTO buscarDespesaFixa(Long id) {
        RecurringExpense recurringExpense = recurringExpenseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Despesa fixa não encontrada com ID: " + id));
        
        return mapToDTO(recurringExpense);
    }
    
    /**
     * Cria uma nova despesa fixa
     */
    public RecurringExpenseResponseDTO criarDespesaFixa(RecurringExpenseRequestDTO requestDTO) {
        try {
            RecurringExpense recurringExpense = new RecurringExpense();
            updateFromDTO(recurringExpense, requestDTO);
            
            RecurringExpense saved = recurringExpenseRepository.save(recurringExpense);
            logger.info("Despesa fixa criada com ID: {}", saved.getId());
            
            return mapToDTO(saved);
        } catch (Exception e) {
            logger.error("Erro ao criar despesa fixa: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao criar despesa fixa: " + e.getMessage());
        }
    }
    
    /**
     * Atualiza uma despesa fixa existente
     */
    public RecurringExpenseResponseDTO atualizarDespesaFixa(Long id, RecurringExpenseRequestDTO requestDTO) {
        try {
            RecurringExpense recurringExpense = recurringExpenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Despesa fixa não encontrada com ID: " + id));
            
            updateFromDTO(recurringExpense, requestDTO);
            
            RecurringExpense saved = recurringExpenseRepository.save(recurringExpense);
            logger.info("Despesa fixa atualizada com ID: {}", saved.getId());
            
            return mapToDTO(saved);
        } catch (Exception e) {
            logger.error("Erro ao atualizar despesa fixa: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao atualizar despesa fixa: " + e.getMessage());
        }
    }
    
    /**
     * Desativa (soft delete) uma despesa fixa
     */
    public void desativarDespesaFixa(Long id) {
        try {
            RecurringExpense recurringExpense = recurringExpenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Despesa fixa não encontrada com ID: " + id));
            
            recurringExpense.setActive(false);
            recurringExpenseRepository.save(recurringExpense);
            
            logger.info("Despesa fixa desativada com ID: {}", id);
        } catch (Exception e) {
            logger.error("Erro ao desativar despesa fixa: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao desativar despesa fixa: " + e.getMessage());
        }
    }
    
    /**
     * Exclui permanentemente uma despesa fixa
     */
    public void excluirDespesaFixa(Long id) {
        try {
            if (!recurringExpenseRepository.existsById(id)) {
                throw new RuntimeException("Despesa fixa não encontrada com ID: " + id);
            }
            
            recurringExpenseRepository.deleteById(id);
            logger.info("Despesa fixa excluída com ID: {}", id);
        } catch (Exception e) {
            logger.error("Erro ao excluir despesa fixa: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao excluir despesa fixa: " + e.getMessage());
        }
    }
    
    /**
     * Gera despesas para um período baseadas nas despesas fixas
     */
    @Transactional
    public List<ExpenseResponseDTO> gerarDespesasParaPeriodo(LocalDate inicio, LocalDate fim) {
        try {
            // Buscar todas as despesas fixas ativas que se aplicam ao período
            List<RecurringExpense> despesasFixas = 
                recurringExpenseRepository.findActiveRecurringExpensesInPeriod(inicio, fim);
            
            logger.info("Gerando despesas para o período de {} a {} com base em {} despesas fixas", 
                inicio, fim, despesasFixas.size());
            
            List<Expense> despesasGeradas = new ArrayList<>();
            
            // Para cada despesa fixa, gerar as ocorrências no período
            for (RecurringExpense despesaFixa : despesasFixas) {
                List<LocalDate> datasDespesa = calcularDatasNoIntervalo(despesaFixa, inicio, fim);
                
                for (LocalDate data : datasDespesa) {
                    // Verificar se a despesa já existe
                    boolean despesaExiste = expenseRepository.existsByDateAndRecurringExpenseId(data, despesaFixa.getId());
                    
                    if (!despesaExiste) {
                        // Criar uma despesa normal para cada data de ocorrência
                        Expense despesa = new Expense(
                            despesaFixa.getDescription(),
                            despesaFixa.getCategory(),
                            data,
                            despesaFixa.getAmount(),
                            false, // Inicialmente não paga
                            despesaFixa.getId() // Referência à despesa recorrente
                        );
                        
                        despesasGeradas.add(despesa);
                    }
                }
            }
            
            // Salvar apenas as despesas novas
            List<Expense> salvas = expenseRepository.saveAll(despesasGeradas);
            logger.info("{} despesas geradas e salvas", salvas.size());
            
            // Buscar todas as despesas do período (incluindo as recém-geradas)
            List<Expense> todasDespesas = expenseRepository.findByDateBetween(inicio, fim);
            
            // Converter para DTO e retornar
            return todasDespesas.stream()
                .map(d -> {
                    RecurringExpense recurringExpense = null;
                    if (d.getRecurringExpenseId() != null) {
                        recurringExpense = recurringExpenseRepository.findById(d.getRecurringExpenseId())
                            .orElse(null);
                    }
                    
                    return new ExpenseResponseDTO(
                        d.getId(),
                        d.getDescription(),
                        d.getCategory(),
                        d.getDate(),
                        d.getAmount(),
                        d.getPaid(),
                        d.getRecurringExpenseId(),
                        recurringExpense != null ? recurringExpense.getRecurrenceInfo() : null,
                        d.getRecurringExpenseId() != null ? "RECURRING" : "REGULAR"
                    );
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Erro ao gerar despesas para o período: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar despesas para o período: " + e.getMessage());
        }
    }
    
    /**
     * Calcula as datas em que uma despesa fixa ocorre em um intervalo
     */
    private List<LocalDate> calcularDatasNoIntervalo(RecurringExpense despesa, LocalDate inicio, LocalDate fim) {
        List<LocalDate> datas = new ArrayList<>();
        
        // Ajustar datas de início e fim
        LocalDate startDate = despesa.getStartDate().isBefore(inicio) ? inicio : despesa.getStartDate();
        LocalDate endDate = despesa.getEndDate() != null && despesa.getEndDate().isBefore(fim) ? 
                            despesa.getEndDate() : fim;
                            
        if (startDate.isAfter(endDate)) {
            return datas; // Intervalo inválido
        }
        
        LocalDate dataAtual = startDate;
        while (!dataAtual.isAfter(endDate)) {
            boolean gerarDespesa = false;
            Integer recurrenceValue = despesa.getRecurrenceValue() != null ? despesa.getRecurrenceValue() : 1;
            
            switch (despesa.getRecurrenceType()) {
                case DAILY:
                    gerarDespesa = true;
                    break;
                    
                case WEEKLY:
                    // Para recorrência semanal, verificamos se o dia da semana está na máscara
                    // Dia da semana é 1 (segunda) a 7 (domingo) em Java
                    int diaDaSemana = dataAtual.getDayOfWeek().getValue();
                    // Ajustar para 0 (domingo) a 6 (sábado)
                    int diaBit = (diaDaSemana == 7) ? 0 : diaDaSemana;
                    gerarDespesa = (recurrenceValue & (1 << diaBit)) != 0;
                    break;
                    
                case MONTHLY:
                    if (recurrenceValue == -1) {
                        // Último dia do mês
                        gerarDespesa = dataAtual.getDayOfMonth() == dataAtual.lengthOfMonth();
                    } else {
                        // Dia específico do mês - usar o mesmo dia da data de início quando recorrenceValue é nulo
                        if (despesa.getRecurrenceValue() == null) {
                            gerarDespesa = dataAtual.getDayOfMonth() == startDate.getDayOfMonth();
                        } else {
                            gerarDespesa = recurrenceValue == dataAtual.getDayOfMonth();
                        }
                    }
                    break;
                    
                case YEARLY:
                    // Para simplicidade, consideramos o dia do ano
                    if (despesa.getRecurrenceValue() == null) {
                        // Se não tiver valor específico, usar o mesmo dia do ano da data de início
                        gerarDespesa = dataAtual.getDayOfYear() == startDate.getDayOfYear();
                    } else {
                        gerarDespesa = recurrenceValue == dataAtual.getDayOfYear();
                    }
                    break;
                
                default:
                    break;
            }
            
            if (gerarDespesa) {
                datas.add(dataAtual);
            }
            
            dataAtual = dataAtual.plusDays(1);
        }
        
        return datas;
    }
    
    /**
     * Converte entidade para DTO
     */
    private RecurringExpenseResponseDTO mapToDTO(RecurringExpense entity) {
        return new RecurringExpenseResponseDTO(
            entity.getId(),
            entity.getDescription(),
            entity.getCategory(),
            entity.getAmount(),
            entity.getStartDate(),
            entity.getEndDate(),
            entity.getRecurrenceType(),
            entity.getRecurrenceValue(),
            entity.getActive(),
            "RECURRING" // Adicionando o tipo RECURRING para despesas fixas
        );
    }
    
    /**
     * Atualiza entidade a partir de um DTO
     */
    private void updateFromDTO(RecurringExpense entity, RecurringExpenseRequestDTO dto) {
        entity.setDescription(dto.getDescription());
        entity.setCategory(dto.getCategory());
        entity.setAmount(dto.getAmount());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setRecurrenceType(dto.getRecurrenceType());
        
        // Para recorrência mensal, se o valor for nulo, use o dia da data de início
        if (dto.getRecurrenceType() == RecurrenceType.MONTHLY && dto.getRecurrenceValue() == null) {
            // Use o dia da data de início como valor de recorrência
            entity.setRecurrenceValue(dto.getStartDate().getDayOfMonth());
            logger.info("Usando o dia da data de início ({}) como valor de recorrência mensal", 
                        dto.getStartDate().getDayOfMonth());
        } else {
            // Caso contrário, use o valor exato informado no DTO
            entity.setRecurrenceValue(dto.getRecurrenceValue());
        }
        
        entity.setActive(true);
    }
}
