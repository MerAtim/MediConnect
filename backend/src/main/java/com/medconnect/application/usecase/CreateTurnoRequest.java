package com.medconnect.application.usecase;

import java.time.LocalDateTime;

public class CreateTurnoRequest {
    private LocalDateTime fechaHora;
    private String especialidad;
    private Long medicoId;
    private Long pacienteId;
    private String preparacion;

    public CreateTurnoRequest(LocalDateTime fechaHora, String especialidad, Long medicoId, Long pacienteId) {
        this(fechaHora, especialidad, medicoId, pacienteId, null);
    }

    public CreateTurnoRequest(LocalDateTime fechaHora, String especialidad, Long medicoId, Long pacienteId, String preparacion) {
        this.fechaHora = fechaHora;
        this.especialidad = especialidad;
        this.medicoId = medicoId;
        this.pacienteId = pacienteId;
        this.preparacion = preparacion;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public Long getMedicoId() {
        return medicoId;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public String getPreparacion() {
        return preparacion;
    }
}
