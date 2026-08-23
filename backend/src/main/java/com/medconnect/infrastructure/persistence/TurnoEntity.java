package com.medconnect.infrastructure.persistence;

import com.medconnect.domain.model.TurnoEstado;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "turnos")
public class TurnoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaHora;
    private String especialidad;
    private Long medicoId;
    private Long pacienteId;

    @Enumerated(EnumType.STRING)
    private TurnoEstado estado;

    private String preparacion;

    protected TurnoEntity() {}

    public TurnoEntity(Long id, LocalDateTime fechaHora, String especialidad, Long medicoId, Long pacienteId, TurnoEstado estado, String preparacion) {
        this.id = id;
        this.fechaHora = fechaHora;
        this.especialidad = especialidad;
        this.medicoId = medicoId;
        this.pacienteId = pacienteId;
        this.estado = estado;
        this.preparacion = preparacion;
    }

    public Long getId() {
        return id;
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

    public TurnoEstado getEstado() {
        return estado;
    }

    public String getPreparacion() {
        return preparacion;
    }
}
