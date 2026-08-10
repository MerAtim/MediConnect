package com.medconnect.domain.model;

public class Medico {

    private Long id;
    private String nombre;
    private String especialidad;
    private String matricula;
    private String direccion;
    private String telefono;
    private String email;
    private String contrasena;

    public Medico(Long id, String nombre, String especialidad, String matricula, String direccion, String telefono, String email, String contrasena) {
        this.id = id;
        this.nombre = nombre;
        this.especialidad = especialidad;
        this.matricula = matricula;
        this.direccion = direccion;
        this.telefono = telefono;
        this.email = email;
        this.contrasena = contrasena;
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

    @Override
    public String toString() {
        return "Medico: id: " + id + ", nombre: " + nombre + ", especialidad: " + especialidad + ", matricula: " + matricula
                + ", direccion: " + direccion + ", telefono: " + telefono + ", email: " + email + ", contrasena: "
                + contrasena;
    }

        
}
