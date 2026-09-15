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

    // LOW de la re-auditoria e2e (2026-09-08, segunda ronda): "entidades
    // cascaron (new Medico(id, null, null, null, null, null, null)) como
    // carrier de id, acoplamiento fragil" -- Turno/RegistroClinico
    // necesitan poder referenciar un medico solo por id (sin traer la
    // entidad completa) en varios puntos: al crear un turno/registro desde
    // un request que solo trae el id, o al reconstruir desde persistencia
    // sin resolver el nombre (evita el N+1 que ya se resolvio para
    // listados via buscarPorIds -- aca el nombre ni se necesita). Antes
    // cada uno de esos 4 sitios repetia la lista completa de null
    // posicionales a mano; si el constructor gana un campo nuevo, hay que
    // tocar los 4 en vez de solo este metodo.
    public static Medico conId(Long id) {
        return new Medico(id, null, null, null, null, null, null);
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
