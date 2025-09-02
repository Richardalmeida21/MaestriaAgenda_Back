package com.maestria.agenda.servico;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Duration;
import java.util.Objects;

@Entity
@Table(name = "servico")
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome do serviço não pode ser vazio")
    @Column(nullable = false, unique = true)
    private String nome;

    @NotNull(message = "Valor do serviço não pode ser nulo")
    @Positive(message = "Valor do serviço deve ser positivo")
    @Column(nullable = false)
    private Double valor;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @NotNull(message = "Duração do serviço não pode ser nula")
    @Column(nullable = false)
    private String duracao; // ISO-8601 format (PT1H30M)

    @Positive(message = "Percentual de comissão deve ser positivo")
    @Column(nullable = true) // Temporariamente nullable para migração
    private Double comissaoPercentual; // Percentual de comissão específico deste serviço

    // Construtor padrão
    public Servico() {}

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getDuracao() {
        return duracao;
    }

    public void setDuracao(String duracao) {
        this.duracao = duracao;
    }

    public Double getComissaoPercentual() {
        return comissaoPercentual;
    }

    public void setComissaoPercentual(Double comissaoPercentual) {
        this.comissaoPercentual = comissaoPercentual;
    }
    
    /**
     * Converte a String de duração para um objeto Duration do Java
     * @return objeto Duration representando a duração do serviço
     */
    public Duration getDuracaoAsObject() {
        return Duration.parse(duracao);
    }
    
    /**
     * Retorna a duração do serviço formatada como texto amigável
     * @return String com duração formatada (ex: "1h 30min")
     */
    public String getDuracaoFormatada() {
        Duration duration = getDuracaoAsObject();
        long horas = duration.toHours();
        long minutos = duration.toMinutesPart();
        
        if (horas > 0) {
            return horas + "h" + (minutos > 0 ? " " + minutos + "min" : "");
        } else {
            return minutos + "min";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Servico servico = (Servico) o;
        return Objects.equals(id, servico.id) &&
               Objects.equals(nome, servico.nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome);
    }

    @Override
    public String toString() {
        return "Servico{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", valor=" + valor +
                ", duracao='" + duracao + '\'' +
                '}';
    }
}