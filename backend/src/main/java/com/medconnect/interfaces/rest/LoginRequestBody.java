package com.medconnect.interfaces.rest;

public class LoginRequestBody {

    private String email;
    private String contrasena;

    public LoginRequestBody() {}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
