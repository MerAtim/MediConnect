package com.medconnect.interfaces.rest;

public class RegistroResponse {
    private Long id;

    public RegistroResponse() {}

    public RegistroResponse(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
