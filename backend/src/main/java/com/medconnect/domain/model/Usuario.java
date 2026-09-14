package com.medconnect.domain.model;

public class Usuario {

    private Long id;
    private String nombre;
    private Email email;
    private String contrasena;
    private UsuarioRole role;

    public Usuario(Long id, String nombre, String email, String contrasena, UsuarioRole role) {
        this.id = id;
        this.nombre = nombre;
        this.email = new Email(email);
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

    public Email getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = new Email(email);
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

    // LOW de la re-auditoria e2e (2026-09-08, segunda ronda): toString()
    // incluia el hash bcrypt de la contrasena en texto plano. Nada del
    // codigo actual llama a Usuario.toString() en un log (se verifico con
    // grep), pero es un riesgo latente: cualquier futuro log.debug("{}",
    // usuario), mensaje de excepcion que interpole el objeto, o volcado en
    // un debugger expondria el hash.
    @Override
    public String toString() {
        return "Usuario: id: " + id + ", nombre: " + nombre + ", email: " + email + ", contrasena: ***"
                + ", role: " + role;
    }

}
