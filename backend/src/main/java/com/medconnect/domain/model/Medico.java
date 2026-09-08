package com.medconnect.domain.model;

public class Medico {

    private Long id;
    private String nombre;
    private String especialidad;
    private String matricula;
    private String direccion;
    private String telefono;
    private Email email;

    public Medico(Long id, String nombre, String especialidad, String matricula, String direccion, String telefono, String email) {
        this.id = id;
        this.nombre = nombre;
        this.especialidad = especialidad;
        this.matricula = matricula;
        this.direccion = direccion;
        this.telefono = telefono;
        this.email = Email.deNullable(email);
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

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = Email.deNullable(email);
    }

    @Override
    public String toString() {
        return "Medico: id: " + id + ", nombre: " + nombre + ", especialidad: " + especialidad + ", matricula: " + matricula
                + ", direccion: " + direccion + ", telefono: " + telefono + ", email: " + email;
    }

        
}
