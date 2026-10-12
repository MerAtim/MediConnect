package com.medconnect.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "especialidad_duraciones")
public class EspecialidadDuracionEntity {

    @Id
    @Column(nullable = false)
    private String especialidad;

    @Column(nullable = false)
    private Integer duracionMinutos;

    protected EspecialidadDuracionEntity() {}

    public EspecialidadDuracionEntity(String especialidad, Integer duracionMinutos) {
        this.especialidad = especialidad;
        this.duracionMinutos = duracionMinutos;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }
}
