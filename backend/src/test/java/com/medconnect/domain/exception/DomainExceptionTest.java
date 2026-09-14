package com.medconnect.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// LOW de la re-auditoria e2e (2026-09-08, segunda ronda): las 7 excepciones
// de dominio ahora comparten esta clase base -- este test confirma que todas
// siguen siendo capturables como DomainException y que el mensaje se
// preserva, sin repetir un test identico por cada subclase.
public class DomainExceptionTest {

    @Test
    public void todasLasExcepcionesDeDominio_sonInstanciaDeDomainException() {
        assertTrue(new TurnoInvalidoException("x") instanceof DomainException);
        assertTrue(new MedicoInvalidoException("x") instanceof DomainException);
        assertTrue(new PacienteInvalidoException("x") instanceof DomainException);
        assertTrue(new UsuarioInvalidoException("x") instanceof DomainException);
        assertTrue(new RegistroClinicoInvalidoException("x") instanceof DomainException);
        assertTrue(new CredencialesInvalidasException("x") instanceof DomainException);
        assertTrue(new DemasiadosIntentosException("x") instanceof DomainException);
    }

    @Test
    public void preservaElMensaje() {
        DomainException ex = new TurnoInvalidoException("mensaje de prueba");
        assertEquals("mensaje de prueba", ex.getMessage());
    }
}
