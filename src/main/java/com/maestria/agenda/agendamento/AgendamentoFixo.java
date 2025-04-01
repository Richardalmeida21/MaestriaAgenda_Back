package com.maestria.agenda.agendamento;

import com.maestria.agenda.cliente.Cliente;
import com.maestria.agenda.profissional.Profissional;
import com.maestria.agenda.servico.Servico;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;

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

    @Column(name = "dia_do_mes", nullable = false)
    private Integer diaDoMes = 1;

    @PrePersist
    @PreUpdate
    private void preparaParaSalvar() {
        // Garante que diaDoMes nunca será nulo
        if (diaDoMes == null) {
            diaDoMes = 1;
        }
    }

    // MODIFICADO: Mudou de int para Integer
    @Column(name = "intervalo_repeticao")
    private Integer intervaloRepeticao = 1; // Por padrão, a cada 1 (dia, semana ou mês)

    // Para repetição DIARIA: não precisa de valor adicional
    // Para repetição SEMANAL: dias da semana (bit flags: 1=domingo, 2=segunda,
    // 4=terça, etc)
    // Para repetição MENSAL: dia do mês (1-31) ou -1 para último dia do mês
    @Column(name = "valor_repeticao")
    private Integer valorRepeticao;

    // Data de início da repetição
    private LocalDate dataInicio;

    // Data final da repetição (pode ser null para "sem fim")
    private LocalDate dataFim;

    // Horário do agendamento
    private LocalTime hora;

    // Removido o campo duracao, pois será obtido do serviço

    @Column(columnDefinition = "TEXT")
    private String observacao;

    // Removido o campo valor, pois será obtido do serviço

    // Enumeração para tipos de repetição
    public enum TipoRepeticao {
        DIARIA, // Repete diariamente (ex: todos os dias, a cada 2 dias, etc)
        SEMANAL, // Repete semanalmente em dias específicos (ex: todas segundas e quartas)
        MENSAL // Repete mensalmente em dias específicos (ex: todo dia 15, último dia do mês)
    }

    // Construtor padrão
    public AgendamentoFixo() {
        // Valores padrão para evitar NullPointerException
        this.diaDoMes = 1;
        this.intervaloRepeticao = 1;
        this.valorRepeticao = 1;
    }

    // Getters e Setters

    public Integer getDiaDoMes() {
        return diaDoMes;
    }

    public void setDiaDoMes(Integer diaDoMes) {
        this.diaDoMes = diaDoMes != null ? diaDoMes : 1;
    }

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

    // MODIFICADO: Alterado para usar Integer
    public Integer getIntervaloRepeticao() {
        return intervaloRepeticao != null ? intervaloRepeticao : 1;
    }

    // MODIFICADO: Alterado para usar Integer e evitar valores nulos
    public void setIntervaloRepeticao(Integer intervaloRepeticao) {
        this.intervaloRepeticao = intervaloRepeticao != null ? intervaloRepeticao : 1;
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

    // Método para obter a duração diretamente do serviço
    public String getDuracao() {
        return servico != null ? servico.getDuracao() : null;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    // Método para obter o valor diretamente do serviço
    public Double getValor() {
        return servico != null ? servico.getValor() : null;
    }

    // Método para obter a duração como objeto Duration
    public Duration getDuracaoAsObject() {
        return servico != null ? servico.getDuracaoAsObject() : Duration.ZERO;
    }

    // Método adicional para cálculos e exibição formatada
    public String getDuracaoFormatada() {
        if (servico == null || servico.getDuracao() == null) {
            return "0min";
        }
        
        Duration d = Duration.parse(servico.getDuracao());
        long horas = d.toHours();
        long minutos = d.toMinutesPart();
        
        if (horas > 0) {
            return horas + "h" + (minutos > 0 ? " " + minutos + "min" : "");
        } else {
            return minutos + "min";
        }
    }

    @Override
    public String toString() {
        return "AgendamentoFixo{" +
                "id=" + id +
                ", cliente=" + (cliente != null ? cliente.getNome() : "null") +
                ", profissional=" + (profissional != null ? profissional.getNome() : "null") +
                ", tipoRepeticao=" + tipoRepeticao +
                ", intervaloRepeticao=" + intervaloRepeticao +
                ", valorRepeticao=" + valorRepeticao +
                ", diaDoMes=" + diaDoMes +
                ", dataInicio=" + dataInicio +
                ", dataFim=" + dataFim +
                ", hora=" + hora +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        AgendamentoFixo that = (AgendamentoFixo) o;
        
        return id != null ? id.equals(that.id) : that.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}