package com.medconnect.application.usecase;

import com.medconnect.domain.model.UsuarioRole;

public class RegistrarUsuarioRequest {
    private String nombre;
    private String email;
    private String contrasena;
    private UsuarioRole role;

    public RegistrarUsuarioRequest(String nombre, String email, String contrasena, UsuarioRole role) {
        this.nombre = nombre;
        this.email = email;
        this.contrasena = contrasena;
        this.role = role;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getContrasena() {
        return contrasena;
    }

    public UsuarioRole getRole() {
        return role;
    }
}
