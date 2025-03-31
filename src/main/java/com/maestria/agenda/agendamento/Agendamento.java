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

    // Alteração: ManyToOne em vez de Enum
    @ManyToOne
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    private LocalDate data;
    private LocalTime hora;

    @Convert(converter = DurationConverter.class)
    private Duration duracao;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @Column(nullable = false)
    private Double valor;

    // Construtor com DadosCadastroAgendamento
    public Agendamento(DadosCadastroAgendamento dados, Cliente cliente, Profissional profissional, Servico servico) {
        this.cliente = cliente;
        this.profissional = profissional;
        this.servico = servico;
        this.data = dados.data();
        this.hora = dados.hora();
        this.duracao = Duration.parse(dados.duracao());
        this.observacao = dados.observacao();
        this.valor = dados.valor();
    }

    // Construtor padrão necessário para o JPA
    public Agendamento() {}

    // Getters e Setters - atualizados para Servico
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

    public Duration getDuracao() {
        return duracao;
    }

    public void setDuracao(Duration duracao) {
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

    // Método para formatar a duração
    public String getDuracaoFormatada() {
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
                ", duracao=" + getDuracaoFormatada() +
                ", observacao='" + observacao + '\'' +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agendamento that = (Agendamento) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(cliente, that.cliente) &&
                Objects.equals(profissional, that.profissional) &&
                Objects.equals(servico, that.servico) &&
                Objects.equals(data, that.data) &&
                Objects.equals(hora, that.hora) &&
                Objects.equals(duracao, that.duracao) &&
                Objects.equals(observacao, that.observacao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cliente, profissional, servico, data, hora, duracao, observacao);
    }
}