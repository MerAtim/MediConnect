package com.medconnect.application.usecase;

public interface ActualizarContrasenaUseCase {

    void cambiarPropia(String email, CambiarContrasenaRequest request);

    boolean resetearComoAdmin(Long usuarioId, ResetearContrasenaRequest request);
}
