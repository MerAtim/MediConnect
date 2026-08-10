package com.medconnect.domain.model;

public class Usuario {

    private Long id;
    private String nombre;
    private String email;
    private String contrasena;
    private UsuarioRole role;

    public Usuario(Long id, String nombre, String email, String contrasena, UsuarioRole role) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.contrasena = contrasena;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

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

    public UsuarioRole getRole() {
        return role;
    }

    public void setRole(UsuarioRole role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "Usuario: id: " + id + ", nombre: " + nombre + ", email: " + email + ", contrasena: " + contrasena
                + ", role: " + role;
    }
    
}
