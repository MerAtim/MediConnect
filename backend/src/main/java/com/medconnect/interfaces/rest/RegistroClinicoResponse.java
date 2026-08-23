package com.medconnect.interfaces.rest;

import java.time.LocalDateTime;

public class RegistroClinicoResponse {

    private Long id;
    private LocalDateTime fecha;
    private Long medicoId;
    private Long pacienteId;
    private String diagnostico;
    private String tratamiento;
    private String observaciones;

    public RegistroClinicoResponse() {}

    public RegistroClinicoResponse(Long id, LocalDateTime fecha, Long medicoId, Long pacienteId, String diagnostico, String tratamiento, String observaciones) {
        this.id = id;
        this.fecha = fecha;
        this.medicoId = medicoId;
        this.pacienteId = pacienteId;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.observaciones = observaciones;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public Long getMedicoId() { return medicoId; }
    public void setMedicoId(Long medicoId) { this.medicoId = medicoId; }
    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
    public String getTratamiento() { return tratamiento; }
    public void setTratamiento(String tratamiento) { this.tratamiento = tratamiento; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
