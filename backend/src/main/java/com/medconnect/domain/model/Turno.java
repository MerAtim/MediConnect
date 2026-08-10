package com.medconnect.domain.model;

public class Turno {

    private Long id;
    private String fecha;
    private String hora;
    private String especialidad;
    private String medico;
    private String paciente;

    public Turno(Long id, String fecha, String hora, String especialidad, String medico, String paciente) {
        this.id = id;
        this.fecha = fecha;
        this.hora = hora;
        this.especialidad = especialidad;
        this.medico = medico;
        this.paciente = paciente;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getMedico() {
        return medico;
    }

    public void setMedico(String medico) {
        this.medico = medico;
    }

    public String getPaciente() {
        return paciente;
    }

    public void setPaciente(String paciente) {
        this.paciente = paciente;
    }

    @Override
    public String toString() {
        return "Turno id: " + id + ", fecha: " + fecha + ", hora: " + hora + ", especialidad: " + especialidad
                + ", medico: " + medico + ", paciente: " + paciente;
    }
        
}