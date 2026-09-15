package com.medconnect.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

// LOW de la re-auditoria e2e (2026-09-08, segunda ronda): "entidades
// cascaron (new Medico(id, null, null, null, null, null, null)) como
// carrier de id, acoplamiento fragil" -- conId() reemplaza la construccion
// manual repetida en los 4 sitios de produccion que solo necesitaban el id
// (CrearTurnoService, CrearRegistroClinicoService, TurnoRepositoryAdapter,
// RegistroClinicoRepositoryAdapter).
public class MedicoTest {

    @Test
    public void conId_devuelveUnMedicoSoloConElId() {
        Medico medico = Medico.conId(5L);

        assertEquals(5L, medico.getId());
        assertNull(medico.getNombre());
        assertNull(medico.getEspecialidad());
        assertNull(medico.getMatricula());
        assertNull(medico.getDireccion());
        assertNull(medico.getTelefono());
        assertNull(medico.getEmail());
    }
}
