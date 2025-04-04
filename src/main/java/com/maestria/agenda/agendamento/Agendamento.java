package com.maestria.agenda.agendamento;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.util.Objects;
import com.maestria.agenda.cliente.Cliente;
import com.maestria.agenda.profissional.Profissional;
import com.maestria.agenda.servico.Servico;
import jakarta.persistence.*;
import com.maestria.agenda.financeiro.PagamentoTipo;

@Entity
@Table(name = "agendamento")
public class Agendamento {

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

    private LocalDate data;
    private LocalTime hora;

    private Long agendamentoFixoId;

    // Removemos o campo duracao, pois será obtido do serviço

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false)
    private PagamentoTipo formaPagamento;

    // Removemos o campo valor, pois será obtido do serviço

    public Agendamento(PagamentoTipo formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public PagamentoTipo getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(PagamentoTipo formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    // Construtor com DadosCadastroAgendamento
    public Agendamento(DadosCadastroAgendamento dados, Cliente cliente, Profissional profissional, Servico servico) {
        this.cliente = cliente;
        this.profissional = profissional;
        this.servico = servico;
        this.data = dados.data();
        this.hora = dados.hora();
        this.observacao = dados.observacao();
        this.formaPagamento = dados.formaPagamento();
    }

    // Construtor padrão necessário para o JPA
    public Agendamento() {
    }

    // Getters e Setters - atualizados para refletir as mudanças

    // Getter
public Long getAgendamentoFixoId() {
    return agendamentoFixoId;
}

// Setter
public void setAgendamentoFixoId(Long agendamentoFixoId) {
    this.agendamentoFixoId = agendamentoFixoId;
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

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    // Método para obter a duração diretamente do serviço
    public Duration getDuracao() {
        return servico != null ? servico.getDuracaoAsObject() : null;
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

    // Método para formatar a duração
    public String getDuracaoFormatada() {
        Duration duracao = getDuracao();
        if (duracao == null) {
            return "0 min";
        }
        long minutos = duracao.toMinutes();
        return minutos + " min";
    }

    @Override
    public String toString() {
        return "Agendamento{" +
                "id=" + id +
                ", cliente=" + cliente +
                ", profissional=" + profissional +
                ", servico=" + (servico != null ? servico.getNome() : "null") +
                ", data=" + data +
                ", hora=" + hora +
                ", observacao='" + observacao + '\'' +
                ", formaPagamento='" + formaPagamento + '\'' +
                "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Agendamento other = (Agendamento) obj;
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
        if (data == null) {
            if (other.data != null)
                return false;
        } else if (!data.equals(other.data))
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
        if (formaPagamento != other.formaPagamento)
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
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        result = prime * result + ((hora == null) ? 0 : hora.hashCode());
        result = prime * result + ((observacao == null) ? 0 : observacao.hashCode());
        result = prime * result + ((formaPagamento == null) ? 0 : formaPagamento.hashCode());
        return result;
    }
}
