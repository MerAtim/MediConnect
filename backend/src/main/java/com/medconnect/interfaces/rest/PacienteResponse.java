package com.medconnect.interfaces.rest;

public class PacienteResponse {

    private Long id;
    private String nombre;
    private String dni;
    private String telefono;
    private String direccion;
    private String obraSocial;
    private String email;

    public PacienteResponse() {}

    public PacienteResponse(Long id, String nombre, String dni, String telefono, String direccion, String obraSocial, String email) {
        this.id = id;
        this.nombre = nombre;
        this.dni = dni;
        this.telefono = telefono;
        this.direccion = direccion;
        this.obraSocial = obraSocial;
        this.email = email;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getObraSocial() { return obraSocial; }
    public void setObraSocial(String obraSocial) { this.obraSocial = obraSocial; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
