package com.maestria.agenda.agendamento;

import com.maestria.agenda.cliente.Cliente;
import com.maestria.agenda.profissional.Profissional;
import com.maestria.agenda.servico.Servico;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "agendamento_fixo")
public class AgendamentoFixo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    @ManyToOne
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    // Tipo de repetição (DIARIA, SEMANAL, MENSAL)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoRepeticao tipoRepeticao;

    // Valor numérico para repetição (intervalo)
    private int intervaloRepeticao = 1; // Por padrão, a cada 1 (dia, semana ou mês)

    // Para repetição DIARIA: não precisa de valor adicional
    // Para repetição SEMANAL: dias da semana (bit flags: 1=domingo, 2=segunda, 4=terça, etc)
    // Para repetição MENSAL: dia do mês (1-31) ou -1 para último dia do mês
    private Integer valorRepeticao;

    // Data de início da repetição
    private LocalDate dataInicio;

    // Data final da repetição (pode ser null para "sem fim")
    private LocalDate dataFim;

    // Horário do agendamento
    private LocalTime hora;

    // Duração no formato ISO-8601 (ex: PT1H30M)
    private String duracao;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    // Valor do serviço
    @Column(nullable = false)
    private Double valor;

    // Enumeração para tipos de repetição
    public enum TipoRepeticao {
        DIARIA,   // Repete diariamente (ex: todos os dias, a cada 2 dias, etc)
        SEMANAL,  // Repete semanalmente em dias específicos (ex: todas segundas e quartas)
        MENSAL    // Repete mensalmente em dias específicos (ex: todo dia 15, último dia do mês)
    }

    // Construtor padrão
    public AgendamentoFixo() {}

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public Servico getServico() {
        return servico;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
    }

    public TipoRepeticao getTipoRepeticao() {
        return tipoRepeticao;
    }

    public void setTipoRepeticao(TipoRepeticao tipoRepeticao) {
        this.tipoRepeticao = tipoRepeticao;
    }

    public int getIntervaloRepeticao() {
        return intervaloRepeticao;
    }

    public void setIntervaloRepeticao(int intervaloRepeticao) {
        this.intervaloRepeticao = intervaloRepeticao;
    }

    public Integer getValorRepeticao() {
        return valorRepeticao;
    }

    public void setValorRepeticao(Integer valorRepeticao) {
        this.valorRepeticao = valorRepeticao;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public String getDuracao() {
        return duracao;
    }

    public void setDuracao(String duracao) {
        this.duracao = duracao;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    @Override
    public String toString() {
        return "AgendamentoFixo{" +
                "id=" + id +
                ", cliente=" + cliente.getNome() +
                ", profissional=" + profissional.getNome() +
                ", tipoRepeticao=" + tipoRepeticao +
                ", dataInicio=" + dataInicio +
                ", dataFim=" + dataFim +
                ", hora=" + hora +
                '}';
    }
}