package com.medconnect.interfaces.rest;

import com.medconnect.domain.model.TurnoEstado;
import java.time.LocalDateTime;

public class TurnoResponse {

    private Long id;
    private LocalDateTime fechaHora;
    private String especialidad;
    private Long medicoId;
    private Long pacienteId;
    private TurnoEstado estado;

    public TurnoResponse() {}

    public TurnoResponse(Long id, LocalDateTime fechaHora, String especialidad, Long medicoId, Long pacienteId, TurnoEstado estado) {
        this.id = id;
        this.fechaHora = fechaHora;
        this.especialidad = especialidad;
        this.medicoId = medicoId;
        this.pacienteId = pacienteId;
        this.estado = estado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
    public Long getMedicoId() { return medicoId; }
    public void setMedicoId(Long medicoId) { this.medicoId = medicoId; }
    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }
    public TurnoEstado getEstado() { return estado; }
    public void setEstado(TurnoEstado estado) { this.estado = estado; }
}
