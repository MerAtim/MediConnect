package com.medconnect.interfaces.rest;

// Picker de medico para la autogestion del PACIENTE (GET /medicos/por-especialidad):
// a diferencia de MedicoResponse, no expone matricula/direccion/telefono/email --
// eso lo sigue viendo solo el admin via el listado paginado normal.
public class MedicoLigeroResponse {

    private Long id;
    private String nombre;

    public MedicoLigeroResponse() {}

    public MedicoLigeroResponse(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
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
}
