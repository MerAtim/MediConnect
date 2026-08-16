package com.medconnect.application.usecase;

public class CreateMedicoResponse {
    private final Long id;

    public CreateMedicoResponse(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
