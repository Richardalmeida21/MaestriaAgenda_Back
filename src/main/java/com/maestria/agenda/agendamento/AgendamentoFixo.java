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

    @Column(name = "forma_pagamento", nullable = false)
    private String formaPagamento;

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    // Enumeração para tipos de repetição
    public enum TipoRepeticao {
        DIARIA, 
        SEMANAL, 
        QUINZENAL,
        MENSAL 
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
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AgendamentoFixo other = (AgendamentoFixo) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (cliente == null) {
            if (other.cliente != null)
                return false;
        } else if (!cliente.equals(other.cliente))
            return false;
        if (profissional == null) {
            if (other.profissional != null)
                return false;
        } else if (!profissional.equals(other.profissional))
            return false;
        if (servico == null) {
            if (other.servico != null)
                return false;
        } else if (!servico.equals(other.servico))
            return false;
        if (tipoRepeticao != other.tipoRepeticao)
            return false;
        if (diaDoMes == null) {
            if (other.diaDoMes != null)
                return false;
        } else if (!diaDoMes.equals(other.diaDoMes))
            return false;
        if (intervaloRepeticao == null) {
            if (other.intervaloRepeticao != null)
                return false;
        } else if (!intervaloRepeticao.equals(other.intervaloRepeticao))
            return false;
        if (valorRepeticao == null) {
            if (other.valorRepeticao != null)
                return false;
        } else if (!valorRepeticao.equals(other.valorRepeticao))
            return false;
        if (dataInicio == null) {
            if (other.dataInicio != null)
                return false;
        } else if (!dataInicio.equals(other.dataInicio))
            return false;
        if (dataFim == null) {
            if (other.dataFim != null)
                return false;
        } else if (!dataFim.equals(other.dataFim))
            return false;
        if (hora == null) {
            if (other.hora != null)
                return false;
        } else if (!hora.equals(other.hora))
            return false;
        if (observacao == null) {
            if (other.observacao != null)
                return false;
        } else if (!observacao.equals(other.observacao))
            return false;
        if (formaPagamento == null) {
            if (other.formaPagamento != null)
                return false;
        } else if (!formaPagamento.equals(other.formaPagamento))
            return false;
        return true;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((cliente == null) ? 0 : cliente.hashCode());
        result = prime * result + ((profissional == null) ? 0 : profissional.hashCode());
        result = prime * result + ((servico == null) ? 0 : servico.hashCode());
        result = prime * result + ((tipoRepeticao == null) ? 0 : tipoRepeticao.hashCode());
        result = prime * result + ((diaDoMes == null) ? 0 : diaDoMes.hashCode());
        result = prime * result + ((intervaloRepeticao == null) ? 0 : intervaloRepeticao.hashCode());
        result = prime * result + ((valorRepeticao == null) ? 0 : valorRepeticao.hashCode());
        result = prime * result + ((dataInicio == null) ? 0 : dataInicio.hashCode());
        result = prime * result + ((dataFim == null) ? 0 : dataFim.hashCode());
        result = prime * result + ((hora == null) ? 0 : hora.hashCode());
        result = prime * result + ((observacao == null) ? 0 : observacao.hashCode());
        result = prime * result + ((formaPagamento == null) ? 0 : formaPagamento.hashCode());
        return result;
    }
}