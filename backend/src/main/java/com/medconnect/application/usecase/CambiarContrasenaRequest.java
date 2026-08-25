package com.medconnect.application.usecase;

public class CambiarContrasenaRequest {
    private String contrasenaActual;
    private String contrasenaNueva;

    public CambiarContrasenaRequest(String contrasenaActual, String contrasenaNueva) {
        this.contrasenaActual = contrasenaActual;
        this.contrasenaNueva = contrasenaNueva;
    }

    public String getContrasenaActual() {
        return contrasenaActual;
    }

    public String getContrasenaNueva() {
        return contrasenaNueva;
    }
}
