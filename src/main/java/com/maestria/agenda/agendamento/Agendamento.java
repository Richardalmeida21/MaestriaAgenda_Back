package com.maestria.agenda.agendamento;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.util.Objects;
import com.maestria.agenda.cliente.Cliente;
import com.maestria.agenda.profissional.Profissional;
import com.maestria.agenda.servico.Servico; 
import jakarta.persistence.*;

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

    // Removemos o campo duracao, pois será obtido do serviço

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @Enumerated(EnumType.STRING)
@Column(name = "forma_pagamento", nullable = false)
private PagamentoTipo formaPagamento;


    // Removemos o campo valor, pois será obtido do serviço

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
    public Agendamento() {}

    // Getters e Setters - atualizados para refletir as mudanças

    public String getFormaPagamento() {
        return formaPagamento;
    }
    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
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
    public boolean equals(Object o) { 
        // Inclua formaPagamento se necessário
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agendamento that = (Agendamento) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(cliente, that.cliente) &&
                Objects.equals(profissional, that.profissional) &&
                Objects.equals(servico, that.servico) &&
                Objects.equals(data, that.data) &&
                Objects.equals(hora, that.hora) &&
                Objects.equals(observacao, that.observacao) &&
                Objects.equals(formaPagamento, that.formaPagamento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cliente, profissional, servico, data, hora, observacao, formaPagamento);
    }
}
