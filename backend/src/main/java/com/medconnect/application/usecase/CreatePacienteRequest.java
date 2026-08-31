package com.medconnect.application.usecase;

import com.medconnect.domain.exception.PacienteInvalidoException;

public class CreatePacienteRequest {
    private String nombre;
    private String dni;
    private String telefono;
    private String direccion;
    private String obraSocial;
    private String numeroAfiliado;
    private String plan;
    private String email;

    public CreatePacienteRequest(String nombre, String dni, String telefono, String direccion, String obraSocial, String numeroAfiliado, String plan, String email) {
        this.nombre = nombre;
        this.dni = dni;
        this.telefono = telefono;
        this.direccion = direccion;
        this.obraSocial = obraSocial;
        this.numeroAfiliado = numeroAfiliado;
        this.plan = plan;
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

    public String getNumeroAfiliado() {
        return numeroAfiliado;
    }

    public String getPlan() {
        return plan;
    }

    public String getEmail() {
        return email;
    }

    // Compartido por CrearPacienteService y ActualizarPacienteService (antes
    // era el mismo bloque de 2 checks copiado en ambos).
    void validar() {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new PacienteInvalidoException("nombre es obligatorio");
        }
        if (dni == null || dni.trim().isEmpty()) {
            throw new PacienteInvalidoException("dni es obligatorio");
        }
    }
}
