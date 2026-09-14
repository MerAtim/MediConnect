package com.medconnect.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UsuarioTest {

    // LOW de la re-auditoria e2e (2026-09-08, segunda ronda): toString()
    // incluia el hash bcrypt de la contrasena en texto plano -- riesgo
    // latente si algun log/excepcion futura llega a interpolar el objeto.
    @Test
    public void toString_noIncluyeElHashDeLaContrasena() {
        Usuario usuario = new Usuario(1L, "Ana Pérez", "ana@medconnect.com",
                "$2a$10$hashBcryptSecretoQueNoDeberiaAparecerEnLogs", UsuarioRole.MEDICO);

        String representacion = usuario.toString();

        assertFalse(representacion.contains("$2a$10$hashBcryptSecretoQueNoDeberiaAparecerEnLogs"));
        assertTrue(representacion.contains("ana@medconnect.com"));
    }
}
