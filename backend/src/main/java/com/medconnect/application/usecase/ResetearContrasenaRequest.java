package com.medconnect.application.usecase;

public class ResetearContrasenaRequest {
    private String contrasenaNueva;

    public ResetearContrasenaRequest(String contrasenaNueva) {
        this.contrasenaNueva = contrasenaNueva;
    }

    public String getContrasenaNueva() {
        return contrasenaNueva;
    }
}
