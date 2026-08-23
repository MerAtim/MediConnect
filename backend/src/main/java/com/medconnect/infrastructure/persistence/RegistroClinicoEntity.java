package com.medconnect.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "registros_clinicos")
public class RegistroClinicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fecha;
    private Long medicoId;
    private Long pacienteId;
    private String diagnostico;
    private String tratamiento;
    private String observaciones;

    protected RegistroClinicoEntity() {}

    public RegistroClinicoEntity(Long id, LocalDateTime fecha, Long medicoId, Long pacienteId, String diagnostico, String tratamiento, String observaciones) {
        this.id = id;
        this.fecha = fecha;
        this.medicoId = medicoId;
        this.pacienteId = pacienteId;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.observaciones = observaciones;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getFecha() {
        return fecha;
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
