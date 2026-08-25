package com.medconnect.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "medicos")
public class MedicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String especialidad;
    private String matricula;
    private String direccion;
    private String telefono;

    @Column(unique = true)
    private String email;

    private Boolean activo = Boolean.TRUE;

    protected MedicoEntity() {}

    public MedicoEntity(Long id, String nombre, String especialidad, String matricula, String direccion, String telefono, String email) {
        this.id = id;
        this.nombre = nombre;
        this.especialidad = especialidad;
        this.matricula = matricula;
        this.direccion = direccion;
        this.telefono = telefono;
        this.email = email;
    }

    public Long getId() {
        return id;
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

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
