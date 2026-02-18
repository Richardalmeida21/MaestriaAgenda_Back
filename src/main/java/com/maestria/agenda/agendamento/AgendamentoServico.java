package com.maestria.agenda.agendamento;

import com.maestria.agenda.servico.Servico;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "agendamento_servico")
public class AgendamentoServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agendamento_id", nullable = false)
    @JsonBackReference
    private Agendamento agendamento;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    @Column(name = "ordem")
    private Integer ordem; // Para manter a ordem dos serviços

    public AgendamentoServico() {
    }

    public AgendamentoServico(Agendamento agendamento, Servico servico, Integer ordem) {
        this.agendamento = agendamento;
        this.servico = servico;
        this.ordem = ordem;
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Agendamento getAgendamento() {
        return agendamento;
    }

    public void setAgendamento(Agendamento agendamento) {
        this.agendamento = agendamento;
    }

    public Servico getServico() {
        return servico;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }
}
