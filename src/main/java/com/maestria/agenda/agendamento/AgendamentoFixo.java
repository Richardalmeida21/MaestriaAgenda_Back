package com.maestria.agenda.agendamento;

import com.maestria.agenda.cliente.Cliente;
import com.maestria.agenda.profissional.Profissional;
import com.maestria.agenda.servico.Servico;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import com.maestria.agenda.financeiro.PagamentoTipo;


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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoRepeticao tipoRepeticao;

    @Column(name = "dia_do_mes", nullable = false)
    private Integer diaDoMes = 1;

    @PrePersist
    @PreUpdate
    private void preparaParaSalvar() {
        if (diaDoMes == null) {
            diaDoMes = 1;
        }
    }

    @Column(name = "intervalo_repeticao")
    private Integer intervaloRepeticao = 1;

    @Column(name = "valor_repeticao")
    private Integer valorRepeticao;

    private LocalDate dataInicio;

    private LocalDate dataFim;

    private LocalTime hora;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @Column(name = "forma_pagamento")
    private String formaPagamento;

    @Column(nullable = false)
    private Boolean ativo = true;

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        if (formaPagamento == null || formaPagamento.trim().isEmpty()) {
            throw new IllegalArgumentException("A forma de pagamento é obrigatória.");
        }
        this.formaPagamento = formaPagamento;
    }

    public enum TipoRepeticao {
        DIARIA, SEMANAL, QUINZENAL, MENSAL
    }

    public AgendamentoFixo() {
        this.diaDoMes = 1;
        this.intervaloRepeticao = 1;
        this.valorRepeticao = 1;
    }

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

    public Integer getIntervaloRepeticao() {
        return intervaloRepeticao != null ? intervaloRepeticao : 1;
    }

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

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public Double getValor() {
        return servico != null ? servico.getValor() : null;
    }

    public Duration getDuracaoAsObject() {
        return servico != null ? servico.getDuracaoAsObject() : Duration.ZERO;
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
                ", formaPagamento='" + formaPagamento + '\'' +
                '}';
    }
}
