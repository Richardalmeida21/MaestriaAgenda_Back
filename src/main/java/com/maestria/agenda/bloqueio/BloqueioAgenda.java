package com.maestria.agenda.bloqueio;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

import com.maestria.agenda.profissional.Profissional;

import jakarta.persistence.*;

@Entity
@Table(name = "bloqueio_agenda")
public class BloqueioAgenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    private LocalDate dataInicio;
    private LocalDate dataFim;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private boolean diaTodo;
    
    @Column(columnDefinition = "TEXT")
    private String motivo;

    // Construtor padrão
    public BloqueioAgenda() {
    }

    // Construtor completo
    public BloqueioAgenda(Profissional profissional, LocalDate dataInicio, LocalDate dataFim, 
            LocalTime horaInicio, LocalTime horaFim, boolean diaTodo, String motivo) {
        this.profissional = profissional;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.diaTodo = diaTodo;
        this.motivo = motivo;
    }

    // Getters e Setters
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

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFim() {
        return horaFim;
    }

    public void setHoraFim(LocalTime horaFim) {
        this.horaFim = horaFim;
    }

    public boolean isDiaTodo() {
        return diaTodo;
    }

    public void setDiaTodo(boolean diaTodo) {
        this.diaTodo = diaTodo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BloqueioAgenda that = (BloqueioAgenda) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "BloqueioAgenda{" +
                "id=" + id +
                ", profissional=" + profissional.getNome() +
                ", dataInicio=" + dataInicio +
                ", dataFim=" + dataFim +
                ", horaInicio=" + horaInicio +
                ", horaFim=" + horaFim +
                ", diaTodo=" + diaTodo +
                ", motivo='" + motivo + '\'' +
                '}';
    }
}