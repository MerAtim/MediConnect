package com.medconnect.application.usecase;

public interface RegistrarUsuarioUseCase {
    RegistrarUsuarioResponse registrar(RegistrarUsuarioRequest request);

    RegistrarUsuarioResponse registrarComoAdmin(RegistrarUsuarioRequest request);
}
