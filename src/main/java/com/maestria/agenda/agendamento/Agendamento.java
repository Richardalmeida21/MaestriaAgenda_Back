package com.maestria.agenda.agendamento;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import com.maestria.agenda.cliente.Cliente;
import com.maestria.agenda.profissional.Profissional;
import com.maestria.agenda.servicos.Servicos;
import jakarta.persistence.*;

@Entity
@Table(name = "agendamento") // Corrigido para letra minúscula por convenção
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    @Enumerated(EnumType.STRING)
    private Servicos servico;

    private LocalDate data;
    private LocalTime hora;

    // Construtor atualizado com os tipos corretos
    public Agendamento(DadosCadastroAgendamento dados, Cliente cliente, Profissional profissional) {
        this.cliente = cliente;
        this.profissional = profissional;
        this.servico = dados.servico();
        this.data = dados.data();
        this.hora = dados.hora();
    }

    public Agendamento() {}

    // Método para atualizar os dados do agendamento
    public void atualizarDados(DadosCadastroAgendamento dados, Cliente cliente, Profissional profissional) {
    this.cliente = cliente;
    this.profissional = profissional;
    this.servico = dados.servico();
    this.data = dados.data();
    this.hora = dados.hora();
}


    // Getters e Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public Servicos getServico() {
        return servico;
    }

    public void setServico(Servicos servico) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agendamento that = (Agendamento) o;
        return id == that.id &&
               Objects.equals(cliente, that.cliente) &&
               Objects.equals(profissional, that.profissional) &&
               servico == that.servico &&
               Objects.equals(data, that.data) &&
               Objects.equals(hora, that.hora);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cliente, profissional, servico, data, hora);
    }

    @Override
    public String toString() {
        return "Agendamento{" +
                "id=" + id +
                ", cliente=" + cliente.getNome() +
                ", profissional=" + profissional.getNome() +
                ", servico=" + servico +
                ", data=" + data +
                ", hora=" + hora +
                '}';
    }
}
