package com.medconnect.application.usecase;

public class CreateTurnoResponse {
    private final Long id;

    public CreateTurnoResponse(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
