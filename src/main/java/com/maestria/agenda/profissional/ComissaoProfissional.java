package com.maestria.agenda.profissional;

import com.maestria.agenda.servico.CategoriaServico;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "comissao_profissional", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "profissional_id", "categoria_id" })
})
public class ComissaoProfissional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    @ManyToOne(optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaServico categoria;

    @NotNull(message = "Percentual de comissão é obrigatório")
    @PositiveOrZero(message = "Percentual deve ser positivo ou zero")
    @Column(nullable = false)
    private Double percentual;

    public ComissaoProfissional() {
    }

    public ComissaoProfissional(Profissional profissional, CategoriaServico categoria, Double percentual) {
        this.profissional = profissional;
        this.categoria = categoria;
        this.percentual = percentual;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public CategoriaServico getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaServico categoria) {
        this.categoria = categoria;
    }

    public Double getPercentual() {
        return percentual;
    }

    public void setPercentual(Double percentual) {
        this.percentual = percentual;
    }
}
