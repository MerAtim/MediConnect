package com.medconnect.domain.model;

public class Paciente {

    private Long id;
    private String nombre;
    private Dni dni;
    private String telefono;
    private String direccion;
    private String obraSocial;
    private String numeroAfiliado;
    private String plan;
    private Email email;

    public Paciente(Long id, String nombre, String dni, String telefono, String direccion, String obraSocial, String numeroAfiliado, String plan, String email) {
        this.id = id;
        this.nombre = nombre;
        this.dni = Dni.deNullable(dni);
        this.telefono = telefono;
        this.direccion = direccion;
        this.obraSocial = obraSocial;
        this.numeroAfiliado = numeroAfiliado;
        this.plan = plan;
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

    public Dni getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = Dni.deNullable(dni);
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getObraSocial() {
        return obraSocial;
    }

    public void setObraSocial(String obraSocial) {
        this.obraSocial = obraSocial;
    }

    public String getNumeroAfiliado() {
        return numeroAfiliado;
    }

    public void setNumeroAfiliado(String numeroAfiliado) {
        this.numeroAfiliado = numeroAfiliado;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = Email.deNullable(email);
    }

    @Override
    public String toString() {
        return "Paciente: id: " + id + ", nombre: " + nombre + ", dni: " + dni + ", telefono: " + telefono + ", direccion: " + direccion + ", obraSocial: " + obraSocial + ", numeroAfiliado: " + numeroAfiliado + ", plan: " + plan + ", email: " + email;
    }

}
