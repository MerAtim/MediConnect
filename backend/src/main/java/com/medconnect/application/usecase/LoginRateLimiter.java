package com.medconnect.application.usecase;

public interface LoginRateLimiter {

    void verificarPermitido(String email);

    void registrarFallo(String email);

    void registrarExito(String email);
}
