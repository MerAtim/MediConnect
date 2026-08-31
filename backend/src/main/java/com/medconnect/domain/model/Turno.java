package com.medconnect.domain.model;

import com.medconnect.domain.exception.TurnoInvalidoException;

import java.time.LocalDateTime;

public class Turno {

    private Long id;
    private LocalDateTime fechaHora;
    private String especialidad;
    private Medico medico;
    private Paciente paciente;
    private TurnoEstado estado;
    private String preparacion;

    public Turno(Long id, LocalDateTime fechaHora, String especialidad, Medico medico, Paciente paciente, TurnoEstado estado) {
        this.id = id;
        this.fechaHora = fechaHora;
        this.especialidad = especialidad;
        this.medico = medico;
        this.paciente = paciente;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public Medico getMedico() {
        return medico;
    }

    public void setMedico(Medico medico) {
        this.medico = medico;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public TurnoEstado getEstado() {
        return estado;
    }

    // Unico punto de mutacion del estado: garantiza la invariante "un turno
    // cancelado no se puede modificar" sin depender de que cada caller la
    // recuerde reimplementar (antes era un chequeo suelto en el use case,
    // saltable por cualquier otro codigo con una referencia a este Turno).
    public void cambiarEstado(TurnoEstado nuevoEstado) {
        if (this.estado == TurnoEstado.CANCELADO) {
            throw new TurnoInvalidoException("No se puede modificar un turno cancelado");
        }
        this.estado = nuevoEstado;
    }

    public String getPreparacion() {
        return preparacion;
    }

    public void setPreparacion(String preparacion) {
        this.preparacion = preparacion;
    }

    @Override
    public String toString() {
        return "Turno id: " + id + ", fechaHora: " + fechaHora + ", especialidad: " + especialidad + ", medico: " + medico
                + ", paciente: " + paciente + ", estado: " + estado;
    }   
        
}