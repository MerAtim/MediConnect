package com.medconnect.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EmailTest {

    @Test
    public void constructor_aceptaFormatoValido() {
        Email email = new Email("ana@medconnect.com");
        assertEquals("ana@medconnect.com", email.getValor());
    }

    @Test
    public void constructor_lanzaExcepcion_siNoTieneArroba() {
        assertThrows(IllegalArgumentException.class, () -> new Email("ana-medconnect.com"));
    }

    @Test
    public void constructor_lanzaExcepcion_siNoTieneDominio() {
        assertThrows(IllegalArgumentException.class, () -> new Email("ana@medconnect"));
    }

    @Test
    public void constructor_lanzaExcepcion_siTieneEspacios() {
        assertThrows(IllegalArgumentException.class, () -> new Email("ana perez@medconnect.com"));
    }

    @Test
    public void constructor_lanzaExcepcion_siEsNull() {
        assertThrows(IllegalArgumentException.class, () -> new Email(null));
    }

    @Test
    public void esFormatoValido_noLanza_soloInforma() {
        assertTrue(Email.esFormatoValido("ana@medconnect.com"));
        assertFalse(Email.esFormatoValido("no-es-un-email"));
        assertFalse(Email.esFormatoValido(null));
    }

    @Test
    public void deNullable_devuelveNull_siEsNullOBlanco() {
        assertNull(Email.deNullable(null));
        assertNull(Email.deNullable(""));
        assertNull(Email.deNullable("   "));
    }

    @Test
    public void deNullable_recortaEspacios() {
        assertEquals("ana@medconnect.com", Email.deNullable("  ana@medconnect.com  ").getValor());
    }

    @Test
    public void deNullable_lanzaExcepcion_siNoEsNullNiBlanco_yElFormatoEsInvalido() {
        assertThrows(IllegalArgumentException.class, () -> Email.deNullable("no-es-un-email"));
    }

    @Test
    public void equals_esPorValor() {
        assertEquals(new Email("ana@medconnect.com"), new Email("ana@medconnect.com"));
        assertNotEquals(new Email("ana@medconnect.com"), new Email("otra@medconnect.com"));
    }

    @Test
    public void hashCode_esConsistenteConEquals() {
        assertEquals(new Email("ana@medconnect.com").hashCode(), new Email("ana@medconnect.com").hashCode());
    }

    @Test
    public void toString_devuelveElValorCrudo() {
        assertEquals("ana@medconnect.com", new Email("ana@medconnect.com").toString());
    }
}
