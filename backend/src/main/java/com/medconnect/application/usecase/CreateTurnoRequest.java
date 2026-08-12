package com.medconnect.application.usecase;

import java.time.LocalDateTime;

public class CreateTurnoRequest {
    private LocalDateTime fechaHora;
    private String especialidad;
    private Long medicoId;
    private Long pacienteId;

    public CreateTurnoRequest(LocalDateTime fechaHora, String especialidad, Long medicoId, Long pacienteId) {
        this.fechaHora = fechaHora;
        this.especialidad = especialidad;
        this.medicoId = medicoId;
        this.pacienteId = pacienteId;
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
}
