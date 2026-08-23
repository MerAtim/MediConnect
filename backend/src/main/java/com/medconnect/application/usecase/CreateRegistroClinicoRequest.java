package com.medconnect.application.usecase;

public class CreateRegistroClinicoRequest {
    private Long medicoId;
    private Long pacienteId;
    private String diagnostico;
    private String tratamiento;
    private String observaciones;

    public CreateRegistroClinicoRequest(Long medicoId, Long pacienteId, String diagnostico, String tratamiento, String observaciones) {
        this.medicoId = medicoId;
        this.pacienteId = pacienteId;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.observaciones = observaciones;
    }

    public Long getMedicoId() {
        return medicoId;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public String getTratamiento() {
        return tratamiento;
    }

    public String getObservaciones() {
        return observaciones;
    }
}
