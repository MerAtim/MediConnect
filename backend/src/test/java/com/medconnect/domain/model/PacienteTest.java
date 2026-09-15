package com.medconnect.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

// LOW de la re-auditoria e2e (2026-09-08, segunda ronda): mismo caso que
// MedicoTest.conId_devuelveUnMedicoSoloConElId -- ver ese comentario.
public class PacienteTest {

    @Test
    public void conId_devuelveUnPacienteSoloConElId() {
        Paciente paciente = Paciente.conId(7L);

        assertEquals(7L, paciente.getId());
        assertNull(paciente.getNombre());
        assertNull(paciente.getDni());
        assertNull(paciente.getTelefono());
        assertNull(paciente.getDireccion());
        assertNull(paciente.getObraSocial());
        assertNull(paciente.getNumeroAfiliado());
        assertNull(paciente.getPlan());
        assertNull(paciente.getEmail());
    }
}
