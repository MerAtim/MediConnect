package com.medconnect.application.usecase;

import com.medconnect.domain.exception.MedicoInvalidoException;

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

    // Compartido por CrearMedicoService y ActualizarMedicoService (antes
    // era el mismo bloque de 3 checks copiado en ambos).
    void validar() {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new MedicoInvalidoException("nombre es obligatorio");
        }
        if (especialidad == null || especialidad.trim().isEmpty()) {
            throw new MedicoInvalidoException("especialidad es obligatoria");
        }
        if (matricula == null || matricula.trim().isEmpty()) {
            throw new MedicoInvalidoException("matricula es obligatoria");
        }
    }
}
