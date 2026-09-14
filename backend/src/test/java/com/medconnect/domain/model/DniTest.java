package com.medconnect.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DniTest {

    @Test
    public void constructor_aceptaSoloDigitos() {
        Dni dni = new Dni("30111222");
        assertEquals("30111222", dni.getValor());
    }

    @Test
    public void constructor_lanzaExcepcion_siTieneLetras() {
        assertThrows(IllegalArgumentException.class, () -> new Dni("30111222A"));
    }

    @Test
    public void constructor_lanzaExcepcion_siTieneEspacios() {
        assertThrows(IllegalArgumentException.class, () -> new Dni("30 111 222"));
    }

    @Test
    public void constructor_lanzaExcepcion_siTieneGuiones() {
        assertThrows(IllegalArgumentException.class, () -> new Dni("30-111-222"));
    }

    @Test
    public void constructor_lanzaExcepcion_siEsNull() {
        assertThrows(IllegalArgumentException.class, () -> new Dni(null));
    }

    // LOW de la re-auditoria e2e (2026-09-08, segunda ronda): el regex
    // original aceptaba cualquier cantidad de digitos sin limite.
    @Test
    public void constructor_aceptaSieteDigitos() {
        Dni dni = new Dni("3011122");
        assertEquals("3011122", dni.getValor());
    }

    @Test
    public void constructor_lanzaExcepcion_siTieneMenosDeSieteDigitos() {
        assertThrows(IllegalArgumentException.class, () -> new Dni("123456"));
    }

    @Test
    public void constructor_lanzaExcepcion_siTieneMasDeOchoDigitos() {
        assertThrows(IllegalArgumentException.class, () -> new Dni("123456789"));
    }

    @Test
    public void esFormatoValido_noLanza_soloInforma() {
        assertTrue(Dni.esFormatoValido("30111222"));
        assertFalse(Dni.esFormatoValido("no-es-un-dni"));
        assertFalse(Dni.esFormatoValido(null));
    }

    @Test
    public void deNullable_devuelveNull_siEsNullOBlanco() {
        assertNull(Dni.deNullable(null));
        assertNull(Dni.deNullable(""));
        assertNull(Dni.deNullable("   "));
    }

    @Test
    public void deNullable_recortaEspacios() {
        assertEquals("30111222", Dni.deNullable("  30111222  ").getValor());
    }

    @Test
    public void deNullable_lanzaExcepcion_siNoEsNullNiBlanco_yElFormatoEsInvalido() {
        assertThrows(IllegalArgumentException.class, () -> Dni.deNullable("no-es-un-dni"));
    }

    @Test
    public void equals_esPorValor() {
        assertEquals(new Dni("30111222"), new Dni("30111222"));
        assertNotEquals(new Dni("30111222"), new Dni("40111222"));
    }

    @Test
    public void hashCode_esConsistenteConEquals() {
        assertEquals(new Dni("30111222").hashCode(), new Dni("30111222").hashCode());
    }

    @Test
    public void toString_devuelveElValorCrudo() {
        assertEquals("30111222", new Dni("30111222").toString());
    }
}
