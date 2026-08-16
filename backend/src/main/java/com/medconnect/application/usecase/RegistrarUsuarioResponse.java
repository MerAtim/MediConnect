package com.medconnect.application.usecase;

public class RegistrarUsuarioResponse {
    private final Long id;

    public RegistrarUsuarioResponse(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
