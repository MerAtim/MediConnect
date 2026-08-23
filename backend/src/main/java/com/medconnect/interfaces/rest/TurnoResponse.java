package com.medconnect.interfaces.rest;

import com.medconnect.domain.model.TurnoEstado;
import java.time.LocalDateTime;

public class TurnoResponse {

    private Long id;
    private LocalDateTime fechaHora;
    private String especialidad;
    private Long medicoId;
    private String medicoNombre;
    private String medicoEspecialidad;
    private Long pacienteId;
    private String pacienteNombre;
    private TurnoEstado estado;
    private String preparacion;

    public TurnoResponse() {}

    public TurnoResponse(Long id, LocalDateTime fechaHora, String especialidad, Long medicoId, String medicoNombre,
                          String medicoEspecialidad, Long pacienteId, String pacienteNombre, TurnoEstado estado, String preparacion) {
        this.id = id;
        this.fechaHora = fechaHora;
        this.especialidad = especialidad;
        this.medicoId = medicoId;
        this.medicoNombre = medicoNombre;
        this.medicoEspecialidad = medicoEspecialidad;
        this.pacienteId = pacienteId;
        this.pacienteNombre = pacienteNombre;
        this.estado = estado;
        this.preparacion = preparacion;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
    public Long getMedicoId() { return medicoId; }
    public void setMedicoId(Long medicoId) { this.medicoId = medicoId; }
    public String getMedicoNombre() { return medicoNombre; }
    public void setMedicoNombre(String medicoNombre) { this.medicoNombre = medicoNombre; }
    public String getMedicoEspecialidad() { return medicoEspecialidad; }
    public void setMedicoEspecialidad(String medicoEspecialidad) { this.medicoEspecialidad = medicoEspecialidad; }
    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }
    public String getPacienteNombre() { return pacienteNombre; }
    public void setPacienteNombre(String pacienteNombre) { this.pacienteNombre = pacienteNombre; }
    public TurnoEstado getEstado() { return estado; }
    public void setEstado(TurnoEstado estado) { this.estado = estado; }
    public String getPreparacion() { return preparacion; }
    public void setPreparacion(String preparacion) { this.preparacion = preparacion; }
}
