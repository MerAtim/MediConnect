package com.medconnect.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pacientes")
public class PacienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;
    private String dni;
    private String telefono;
    private String direccion;
    private String obraSocial;
    private String numeroAfiliado;
    private String plan;

    // Nullable a proposito: un Paciente puede existir sin cuenta de acceso
    // vinculada (decision explicita de HIGH #8 de la re-auditoria e2e --
    // Usuario y Medico/Paciente estan desacoplados). Ver V4__not_null_nombre_y_email_obligatorios.sql.
    @Column(unique = true)
    private String email;

    private Boolean activo = Boolean.TRUE;

    protected PacienteEntity() {}

    public PacienteEntity(Long id, String nombre, String dni, String telefono, String direccion, String obraSocial, String numeroAfiliado, String plan, String email) {
        this.id = id;
        this.nombre = nombre;
        this.dni = dni;
        this.telefono = telefono;
        this.direccion = direccion;
        this.obraSocial = obraSocial;
        this.numeroAfiliado = numeroAfiliado;
        this.plan = plan;
        this.email = email;
    }

    public Long getId() {
        return id;
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

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
