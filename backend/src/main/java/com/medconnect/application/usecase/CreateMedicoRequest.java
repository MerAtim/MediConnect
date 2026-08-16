package com.medconnect.application.usecase;

public class CreateMedicoRequest {
    private String nombre;
    private String especialidad;
    private String matricula;
    private String direccion;
    private String telefono;
    private String email;

    public CreateMedicoRequest(String nombre, String especialidad, String matricula, String direccion, String telefono, String email) {
        this.nombre = nombre;
        this.especialidad = especialidad;
        this.matricula = matricula;
        this.direccion = direccion;
        this.telefono = telefono;
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public String getMatricula() {
        return matricula;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getEmail() {
        return email;
    }
}
