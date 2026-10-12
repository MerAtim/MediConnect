package com.medconnect.interfaces.rest;

public class EspecialidadDuracionResponse {

    private String especialidad;
    private int duracionMinutos;

    public EspecialidadDuracionResponse() {}

    public EspecialidadDuracionResponse(String especialidad, int duracionMinutos) {
        this.especialidad = especialidad;
        this.duracionMinutos = duracionMinutos;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(int duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }
}
