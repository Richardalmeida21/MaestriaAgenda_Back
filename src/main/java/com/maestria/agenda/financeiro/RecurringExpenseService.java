package com.maestria.agenda.financeiro;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        List<RecurringExpense> despesasFixas = recurringExpenseRepository.findAll();
        return despesasFixas.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Lista despesas fixas ativas
     */
    public List<RecurringExpenseResponseDTO> listarDespesasFixasAtivas() {
        List<RecurringExpense> despesasFixas = recurringExpenseRepository.findByActiveTrue();
        return despesasFixas.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca uma despesa fixa por ID
     */
    public RecurringExpenseResponseDTO buscarDespesaFixa(Long id) {
        RecurringExpense despesaFixa = recurringExpenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Despesa fixa não encontrada com ID: " + id));
        return mapToDTO(despesaFixa);
    }
    
    /**
     * Atualiza o status de pagamento de uma despesa fixa
     */
    @Transactional
    public void atualizarStatusPagamento(Long id, boolean paid) {
        try {
            RecurringExpense recurringExpense = recurringExpenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Despesa fixa não encontrada com ID: " + id));
            
            recurringExpense.setPaid(paid);
            recurringExpenseRepository.save(recurringExpense);
            logger.info("Status de pagamento atualizado para despesa fixa ID={}: {}", id, paid);
        } catch (Exception e) {
            logger.error("Erro ao atualizar status de pagamento da despesa fixa: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao atualizar status de pagamento: " + e.getMessage());
        }
    }
    
    /**
     * Cria uma nova despesa fixa
     */
    @Transactional
    public RecurringExpenseCreationResponse criarDespesaFixa(RecurringExpenseRequestDTO requestDTO) {
        try {
            // Validar dados
            if (requestDTO.getAmount() == null || requestDTO.getAmount() <= 0) {
                throw new IllegalArgumentException("O valor da despesa deve ser maior que zero");
            }
            
            if (requestDTO.getRecurrenceType() == null) {
                throw new IllegalArgumentException("O tipo de recorrência é obrigatório");
            }
            
            // Criar a despesa fixa
            RecurringExpense recurringExpense = new RecurringExpense();
            recurringExpense.setDescription(requestDTO.getDescription());
            recurringExpense.setCategory(requestDTO.getCategory());
            recurringExpense.setAmount(requestDTO.getAmount());
            recurringExpense.setRecurrenceType(requestDTO.getRecurrenceType());
            recurringExpense.setRecurrenceValue(requestDTO.getRecurrenceValue());
            recurringExpense.setStartDate(requestDTO.getStartDate());
            recurringExpense.setEndDate(requestDTO.getEndDate());
            recurringExpense.setActive(true);

            // Salvar a despesa fixa
            recurringExpense = recurringExpenseRepository.save(recurringExpense);
            logger.info("Despesa fixa criada com ID: {}", recurringExpense.getId());
            
            // Gerar despesas futuras por 3 meses usando fuso horário de São Paulo
            ZoneId zonaSaoPaulo = ZoneId.of("America/Sao_Paulo");
            LocalDate hoje = LocalDate.now(zonaSaoPaulo);
            LocalDate limiteGeracao = hoje.plusMonths(3);
            LocalDate dataFim = requestDTO.getEndDate() != null && requestDTO.getEndDate().isBefore(limiteGeracao) 
                    ? requestDTO.getEndDate() 
                    : limiteGeracao;
            
            List<Expense> generatedExpenses = generateFutureExpensesInPeriod(recurringExpense, hoje, dataFim);

            // Converter para DTOs
            List<ExpenseResponseDTO> expenseDTOs = generatedExpenses.stream()
                .map(expense -> new ExpenseResponseDTO(
                    expense.getId(),
                    expense.getDescription(),
                    expense.getCategory(),
                    expense.getDate(),
                    expense.getAmount(),
                    expense.getPaid(),
                    true, // isFixo
                    expense.getDayOfMonth(),
                    expense.getEndDate()
                ))
                .collect(Collectors.toList());

            return new RecurringExpenseCreationResponse(
                mapToDTO(recurringExpense),
                expenseDTOs
            );
        } catch (Exception e) {
            logger.error("Erro ao criar despesa fixa: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao criar despesa fixa: " + e.getMessage());
        }
    }
    
    /**
     * Atualiza uma despesa fixa existente
     */
    @Transactional
    public RecurringExpenseResponseDTO atualizarDespesaFixa(Long id, RecurringExpenseRequestDTO requestDTO) {
        try {
            RecurringExpense recurringExpense = recurringExpenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Despesa fixa não encontrada com ID: " + id));
            
            // Guardar valores antigos para verificar o que mudou
            String descricaoAntiga = recurringExpense.getDescription();
            String categoriaAntiga = recurringExpense.getCategory();
            Double valorAntigo = recurringExpense.getAmount();
            RecurrenceType tipoRecorrenciaAntigo = recurringExpense.getRecurrenceType();
            Integer valorRecorrenciaAntigo = recurringExpense.getRecurrenceValue();
            
            // Atualizar a entidade principal
            updateFromDTO(recurringExpense, requestDTO);
            
            RecurringExpense saved = recurringExpenseRepository.save(recurringExpense);
            logger.info("Despesa fixa atualizada com ID: {}", saved.getId());
            
            // Atualizar despesas futuras
            LocalDate hoje = LocalDate.now();
            
            // Buscar todas as instâncias futuras
            List<Expense> despesasFuturas = expenseRepository.findByRecurringExpenseIdAndDateBetween(
                id, hoje, hoje.plusMonths(12));
            
            // Se houver alterações nos valores principais, atualizar todas as instâncias futuras
            boolean atualizarTudo = !descricaoAntiga.equals(saved.getDescription()) ||
                                   !categoriaAntiga.equals(saved.getCategory()) ||
                                   !valorAntigo.equals(saved.getAmount());
            
            // Se o padrão de recorrência mudou, vamos remover tudo e recriar
            boolean padraoMudou = !tipoRecorrenciaAntigo.equals(saved.getRecurrenceType()) ||
                                 (valorRecorrenciaAntigo == null && saved.getRecurrenceValue() != null) ||
                                 (valorRecorrenciaAntigo != null && !valorRecorrenciaAntigo.equals(saved.getRecurrenceValue()));
            
            if (padraoMudou) {
                // Remover todas as instâncias futuras e recriar
                expenseRepository.deleteAll(despesasFuturas);
                
                // Limite de geração (3 meses à frente)
                LocalDate limiteGeracao = hoje.plusMonths(3);
                LocalDate dataFim = saved.getEndDate() != null && saved.getEndDate().isBefore(limiteGeracao) 
                        ? saved.getEndDate() 
                        : limiteGeracao;
                
                // Gerar novas instâncias
                generateFutureExpensesInPeriod(saved, hoje, dataFim);
            } else if (atualizarTudo) {
                // Apenas atualizar os valores das instâncias existentes
                for (Expense despesa : despesasFuturas) {
                    despesa.setDescription(saved.getDescription());
                    despesa.setCategory(saved.getCategory());
                    despesa.setAmount(saved.getAmount());
                    expenseRepository.save(despesa);
                }
            }
            
            return mapToDTO(saved);
        } catch (Exception e) {
            logger.error("Erro ao atualizar despesa fixa: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao atualizar despesa fixa: " + e.getMessage());
        }
    }
    
    /**
     * Desativa (soft delete) uma despesa fixa
     */
    @Transactional
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
    @Transactional
    public void excluirDespesaFixa(Long id) {
        try {
            if (!recurringExpenseRepository.existsById(id)) {
                throw new RuntimeException("Despesa fixa não encontrada com ID: " + id);
            }
            
            // Primeiro excluir todas as despesas geradas a partir desta despesa fixa
            List<Expense> despesasRelacionadas = expenseRepository.findByRecurringExpenseId(id);
            logger.info("Excluindo despesa fixa ID={} e suas {} instâncias", id, despesasRelacionadas.size());
            
            expenseRepository.deleteAll(despesasRelacionadas);
            
            // Depois excluir a despesa fixa em si
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
            List<RecurringExpense> despesasFixasAtivas = recurringExpenseRepository
                .findActiveRecurringExpensesInPeriod(inicio, fim);
            
            logger.info("Gerando despesas para período {}-{} para {} despesas fixas", 
                      inicio, fim, despesasFixasAtivas.size());
            
            List<Expense> despesasGeradas = new ArrayList<>();
            
            for (RecurringExpense despesaFixa : despesasFixasAtivas) {
                List<Expense> geradas = generateFutureExpensesInPeriod(despesaFixa, inicio, fim);
                despesasGeradas.addAll(geradas);
            }
            
            return despesasGeradas.stream()
                .map(d -> new ExpenseResponseDTO(
                    d.getId(), 
                    d.getDescription(), 
                    d.getCategory(), 
                    d.getDate(), 
                    d.getAmount(), 
                    d.getPaid(),
                    true,
                    d.getDayOfMonth(),
                    d.getEndDate()
                ))
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Erro ao gerar despesas para período: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar despesas para período: " + e.getMessage());
        }
    }
    
    /**
     * Gera instâncias de despesas recorrentes para um período específico,
     * indicando quais já existem no banco de dados e quais seriam novas.
     *
     * @param inicio Data inicial do período
     * @param fim Data final do período
     * @return Lista de instâncias de despesas recorrentes
     */
    public List<RecurringExpenseInstanceDTO> gerarInstanciasDespesasFixas(LocalDate inicio, LocalDate fim) {
        try {
            List<RecurringExpense> despesasFixasAtivas = recurringExpenseRepository
                .findActiveRecurringExpensesInPeriod(inicio, fim);
            
            List<RecurringExpenseInstanceDTO> instances = new ArrayList<>();
            
            logger.info("Gerando instâncias potenciais de despesas para período {}-{} para {} despesas fixas", 
                      inicio, fim, despesasFixasAtivas.size());
            
            // Para cada despesa fixa ativa no período, calcular suas datas de ocorrência
            for (RecurringExpense despesaFixa : despesasFixasAtivas) {
                // Calcular datas em que esta despesa ocorre no período
                List<LocalDate> datas = calcularDatasNoIntervalo(despesaFixa, inicio, fim);
                
                for (LocalDate data : datas) {
                    // Verificar se já existe no banco de dados
                    boolean despesaExiste = expenseRepository.existsByDateAndRecurringExpenseId(data, despesaFixa.getId());
                    
                    // Criar DTO para a instância
                    RecurringExpenseInstanceDTO instance = new RecurringExpenseInstanceDTO(
                        null, // ID seria preenchido se já existir e for buscado
                        despesaFixa.getId(),
                        despesaFixa.getDescription(),
                        despesaFixa.getCategory(),
                        despesaFixa.getAmount(),
                        data,
                        false, // Presumindo não paga
                        despesaExiste // Se já existe no banco
                    );
                    
                    instances.add(instance);
                }
            }
            
            // Ordenar por data
            instances.sort(Comparator.comparing(RecurringExpenseInstanceDTO::getDueDate));
            
            return instances;
        } catch (Exception e) {
            logger.error("Erro ao gerar instâncias de despesas fixas: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar instâncias de despesas fixas: " + e.getMessage());
        }
    }
    
    /**
     * Gera despesas futuras para uma despesa fixa dentro de um período específico
     */
    @Transactional
    private List<Expense> generateFutureExpensesInPeriod(RecurringExpense recurringExpense, 
                                                       LocalDate dataInicio, 
                                                       LocalDate dataFim) {
        List<Expense> expenses = new ArrayList<>();
        
        // Calcular datas em que esta despesa ocorre no período
        List<LocalDate> datas = calcularDatasNoIntervalo(recurringExpense, dataInicio, dataFim);
        
        logger.info("Gerando {} despesas futuras para a despesa fixa ID={} de {} até {}", 
                    datas.size(), recurringExpense.getId(), dataInicio, dataFim);
        
        // Para cada data calculada, criar uma despesa específica (se ainda não existir)
        for (LocalDate data : datas) {
            boolean despesaExiste = expenseRepository.existsByDateAndRecurringExpenseId(data, recurringExpense.getId());
            
            if (!despesaExiste) {
                Expense expense = new Expense();
                expense.setDescription(recurringExpense.getDescription());
                expense.setCategory(recurringExpense.getCategory());
                expense.setAmount(recurringExpense.getAmount());
                expense.setDate(data);
                expense.setPaid(false);
                expense.setIsFixo(true);
                expense.setDayOfMonth(recurringExpense.getRecurrenceValue());
                expense.setEndDate(recurringExpense.getEndDate());
                expense.setRecurringExpenseId(recurringExpense.getId());
                
                Expense savedExpense = expenseRepository.save(expense);
                expenses.add(savedExpense);
                
                logger.debug("Despesa gerada para {} com valor {}", data, expense.getAmount());
            }
        }
        
        return expenses;
    }
    
    /**
     * Calcula todas as datas em que uma despesa fixa ocorre dentro de um intervalo
     */
    private List<LocalDate> calcularDatasNoIntervalo(RecurringExpense recurringExpense, 
                                                   LocalDate inicio, LocalDate fim) {
        List<LocalDate> datas = new ArrayList<>();
        
        // Definir a data de início real (a maior entre a data inicial da despesa e a data inicial do período)
        LocalDate dataInicial = recurringExpense.getStartDate().isBefore(inicio) 
            ? inicio 
            : recurringExpense.getStartDate();
        
        // Definir a data de fim real (a menor entre a data final da despesa e a data final do período)
        LocalDate dataFinal = recurringExpense.getEndDate() != null && recurringExpense.getEndDate().isBefore(fim)
            ? recurringExpense.getEndDate()
            : fim;
        
        // Se a data inicial é depois da data final, não há datas a calcular
        if (dataInicial.isAfter(dataFinal)) {
            return datas;
        }
        
        // Calcular as datas baseadas no tipo de recorrência
        switch (recurringExpense.getRecurrenceType()) {
            case DAILY:
                datas = calcularDatasRecorrenciaDiaria(recurringExpense, dataInicial, dataFinal);
                break;
            case WEEKLY:
                datas = calcularDatasRecorrenciaSemanal(recurringExpense, dataInicial, dataFinal);
                break;
            case MONTHLY:
                datas = calcularDatasRecorrenciaMensal(recurringExpense, dataInicial, dataFinal);
                break;
            case YEARLY:
                datas = calcularDatasRecorrenciaAnual(recurringExpense, dataInicial, dataFinal);
                break;
        }
        
        return datas;
    }
    
    /**
     * Calcula datas para recorrência diária
     */
    private List<LocalDate> calcularDatasRecorrenciaDiaria(
            RecurringExpense recurringExpense, LocalDate inicio, LocalDate fim) {
        List<LocalDate> datas = new ArrayList<>();
        
        int intervaloDias = recurringExpense.getRecurrenceValue() != null ? 
                recurringExpense.getRecurrenceValue() : 1;
        
        for (LocalDate data = inicio; 
             !data.isAfter(fim); 
             data = data.plusDays(intervaloDias)) {
            datas.add(data);
        }
        
        return datas;
    }
    
    /**
     * Calcula datas para recorrência semanal
     * O recurrenceValue é uma máscara de bits onde cada bit representa um dia da semana
     * Bit 0 (valor 1) = Domingo, Bit 1 (valor 2) = Segunda, etc.
     */
    private List<LocalDate> calcularDatasRecorrenciaSemanal(
            RecurringExpense recurringExpense, LocalDate inicio, LocalDate fim) {
        List<LocalDate> datas = new ArrayList<>();
        
        int mascara = recurringExpense.getRecurrenceValue() != null ? 
                recurringExpense.getRecurrenceValue() : 0;
        
        // Se a máscara é 0, não há dias selecionados
        if (mascara == 0) {
            return datas;
        }
        
        for (LocalDate data = inicio; !data.isAfter(fim); data = data.plusDays(1)) {
            // Converter do domingo=1 para domingo=0 para verificar o bit correto
            int diaDaSemana = data.getDayOfWeek().getValue() % 7; // Domingo=0, Segunda=1, ...
            
            // Verificar se o bit correspondente está definido na máscara
            if ((mascara & (1 << diaDaSemana)) != 0) {
                datas.add(data);
            }
        }
        
        return datas;
    }
    
    /**
     * Calcula datas para recorrência mensal
     * recurrenceValue é o dia do mês (1-31) ou -1 para o último dia
     */
    private List<LocalDate> calcularDatasRecorrenciaMensal(
            RecurringExpense recurringExpense, LocalDate inicio, LocalDate fim) {
        List<LocalDate> datas = new ArrayList<>();
        
        int diaDoMes = recurringExpense.getRecurrenceValue() != null ? 
                recurringExpense.getRecurrenceValue() : 1;
        
        // Começar no mês da data de início
        int mesInicial = inicio.getMonthValue();
        int anoInicial = inicio.getYear();
        
        // Percorrer meses até a data final
        for (int ano = anoInicial; ano <= fim.getYear(); ano++) {
            int mesInicio = (ano == anoInicial) ? mesInicial : 1;
            int mesFim = (ano == fim.getYear()) ? fim.getMonthValue() : 12;
            
            for (int mes = mesInicio; mes <= mesFim; mes++) {
                YearMonth yearMonth = YearMonth.of(ano, mes);
                
                // Se diaDoMes é -1, usar o último dia do mês
                LocalDate data;
                if (diaDoMes == -1) {
                    data = yearMonth.atEndOfMonth();
                } else {
                    // Garantir que não ultrapassamos o último dia do mês
                    int ultimoDiaDoMes = yearMonth.lengthOfMonth();
                    int diaReal = Math.min(diaDoMes, ultimoDiaDoMes);
                    data = LocalDate.of(ano, mes, diaReal);
                }
                
                // Verificar se a data está no intervalo
                if (!data.isBefore(inicio) && !data.isAfter(fim)) {
                    datas.add(data);
                }
            }
        }
        
        return datas;
    }
    
    /**
     * Calcula datas para recorrência anual
     * recurrenceValue é o dia do ano (1-366)
     */
    private List<LocalDate> calcularDatasRecorrenciaAnual(
            RecurringExpense recurringExpense, LocalDate inicio, LocalDate fim) {
        List<LocalDate> datas = new ArrayList<>();
        
        // Valor padrão: 1º de janeiro
        int diaDoAno = recurringExpense.getRecurrenceValue() != null ? 
                recurringExpense.getRecurrenceValue() : 1;
        
        for (int ano = inicio.getYear(); ano <= fim.getYear(); ano++) {
            // Garantir que não ultrapassamos o último dia do ano
            boolean ehAnoBissexto = LocalDate.of(ano, 1, 1).isLeapYear();
            int ultimoDiaDoAno = ehAnoBissexto ? 366 : 365;
            int diaReal = Math.min(diaDoAno, ultimoDiaDoAno);
            
            LocalDate data = LocalDate.ofYearDay(ano, diaReal);
            
            // Verificar se a data está no intervalo
            if (!data.isBefore(inicio) && !data.isAfter(fim)) {
                datas.add(data);
            }
        }
        
        return datas;
    }
    
    /**
     * Atualiza uma entidade RecurringExpense com os dados do DTO
     */
    private void updateFromDTO(RecurringExpense recurringExpense, RecurringExpenseRequestDTO dto) {
        if (dto.getDescription() != null) {
            recurringExpense.setDescription(dto.getDescription());
        }
        if (dto.getCategory() != null) {
            recurringExpense.setCategory(dto.getCategory());
        }
        if (dto.getAmount() != null) {
            recurringExpense.setAmount(dto.getAmount());
        }
        if (dto.getStartDate() != null) {
            recurringExpense.setStartDate(dto.getStartDate());
        }
        // Aqui estava o erro: tentativa de converter RecurrenceType para LocalDate
        recurringExpense.setEndDate(dto.getEndDate());
        if (dto.getRecurrenceType() != null) {
            recurringExpense.setRecurrenceType(dto.getRecurrenceType());
        }
        if (dto.getRecurrenceValue() != null) {
            recurringExpense.setRecurrenceValue(dto.getRecurrenceValue());
        }
    }
    
    /**
     * Converte uma RecurringExpense para seu DTO
     */
    private RecurringExpenseResponseDTO mapToDTO(RecurringExpense recurringExpense) {
        return new RecurringExpenseResponseDTO(
            recurringExpense.getId(),
            recurringExpense.getDescription(),
            recurringExpense.getCategory(),
            recurringExpense.getAmount(),
            recurringExpense.getStartDate(),
            recurringExpense.getEndDate(),
            recurringExpense.getRecurrenceType(),
            recurringExpense.getRecurrenceValue(),
            recurringExpense.getActive(),
            "RECURRING",
            recurringExpense.getPaid()
        );
    }
}