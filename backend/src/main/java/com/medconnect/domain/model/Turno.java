package com.medconnect.domain.model;

import java.time.LocalDateTime;

import com.medconnect.domain.model.com.medconnect.domain.model.Medico;
import com.medconnect.domain.model.com.medconnect.domain.model.Paciente;

public class Turno {

    private Long id;
    private LocalDateTime fechaHora;
    private Medico medico;
    private Paciente paciente;
    private TurnoEstado estado; //pendiente, confirmado, cancelado

    public Turno(Long id, LocalDateTime fechaHora, Medico medico, Paciente paciente, TurnoEstado estado) {
        this.id = id;
        this.fechaHora = fechaHora;
        this.medico = medico;
        this.paciente = paciente;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public Medico getMedico() {
        return medico;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public TurnoEstado getEstado() {
        return estado;
    }

    public void setEstado(TurnoEstado estado) {
        this.estado = estado;
    }
    
}