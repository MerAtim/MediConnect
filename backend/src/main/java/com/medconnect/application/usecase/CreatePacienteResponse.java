package com.medconnect.application.usecase;

public class CreatePacienteResponse {
    private final Long id;

    public CreatePacienteResponse(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
