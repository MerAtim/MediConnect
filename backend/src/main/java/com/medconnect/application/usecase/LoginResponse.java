package com.medconnect.application.usecase;

import com.medconnect.domain.model.UsuarioRole;

public class LoginResponse {
    private final String token;
    private final Long id;
    private final String nombre;
    private final String email;
    private final UsuarioRole role;

    public LoginResponse(String token, Long id, String nombre, String email, UsuarioRole role) {
        this.token = token;
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public UsuarioRole getRole() {
        return role;
    }
}
