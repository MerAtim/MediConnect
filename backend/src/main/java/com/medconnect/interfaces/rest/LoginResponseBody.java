package com.medconnect.interfaces.rest;

public class LoginResponseBody {
    private String token;
    private Long id;
    private String nombre;
    private String email;
    private String role;

    public LoginResponseBody() {}

    public LoginResponseBody(String token, Long id, String nombre, String email, String role) {
        this.token = token;
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.role = role;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
