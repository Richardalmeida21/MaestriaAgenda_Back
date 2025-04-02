package com.maestria.agenda.financeiro;

public class HorarioData {
    private int hora;
    private int totalAgendamentos;
    private int percentual;

    public HorarioData(int hora, int totalAgendamentos, int percentual) {
        this.hora = hora;
        this.totalAgendamentos = totalAgendamentos;
        this.percentual = percentual;
    }

    public int getHora() {
        return hora;
    }

    public void setHora(int hora) {
        this.hora = hora;
    }

    public int getTotalAgendamentos() {
        return totalAgendamentos;
    }

    public void setTotalAgendamentos(int totalAgendamentos) {
        this.totalAgendamentos = totalAgendamentos;
    }

    public int getPercentual() {
        return percentual;
    }

    public void setPercentual(int percentual) {
        this.percentual = percentual;
    }
}