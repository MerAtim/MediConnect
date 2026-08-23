package com.medconnect.domain.model;

import java.time.LocalDateTime;

public class RegistroClinico {

    private Long id;
    private LocalDateTime fecha;
    private Medico medico;
    private Paciente paciente;
    private String diagnostico;
    private String tratamiento;
    private String observaciones;

    public RegistroClinico(Long id, LocalDateTime fecha, Medico medico, Paciente paciente, String diagnostico, String tratamiento, String observaciones) {
        this.id = id;
        this.fecha = fecha;
        this.medico = medico;
        this.paciente = paciente;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.observaciones = observaciones;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
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

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getTratamiento() {
        return tratamiento;
    }

    public void setTratamiento(String tratamiento) {
        this.tratamiento = tratamiento;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    @Override
    public String toString() {
        return "RegistroClinico id: " + id + ", fecha: " + fecha + ", medico: " + medico + ", paciente: " + paciente
                + ", diagnostico: " + diagnostico + ", tratamiento: " + tratamiento + ", observaciones: " + observaciones;
    }
}
