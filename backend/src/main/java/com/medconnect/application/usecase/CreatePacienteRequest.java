package com.medconnect.application.usecase;

public class CreatePacienteRequest {
    private String nombre;
    private String dni;
    private String telefono;
    private String direccion;
    private String obraSocial;
    private String email;

    public CreatePacienteRequest(String nombre, String dni, String telefono, String direccion, String obraSocial, String email) {
        this.nombre = nombre;
        this.dni = dni;
        this.telefono = telefono;
        this.direccion = direccion;
        this.obraSocial = obraSocial;
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDni() {
        return dni;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getObraSocial() {
        return obraSocial;
    }

    public String getEmail() {
        return email;
    }
}
