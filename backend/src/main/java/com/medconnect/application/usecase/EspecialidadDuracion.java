package com.medconnect.application.usecase;

public class EspecialidadDuracion {

    private final String especialidad;
    private final int duracionMinutos;

    public EspecialidadDuracion(String especialidad, int duracionMinutos) {
        this.especialidad = especialidad;
        this.duracionMinutos = duracionMinutos;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }
}
