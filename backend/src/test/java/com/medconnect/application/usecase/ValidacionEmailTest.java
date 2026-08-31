package com.medconnect.application.usecase;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ValidacionEmailTest {

    private record Registro(Long id) {
    }

    @Test
    public void normalizar_devuelveNull_siEsNullOBlanco() {
        assertNull(ValidacionEmail.normalizar(null));
        assertNull(ValidacionEmail.normalizar(""));
        assertNull(ValidacionEmail.normalizar("   "));
    }

    @Test
    public void normalizar_recortaEspacios() {
        assertEquals("ana@medconnect.com", ValidacionEmail.normalizar("  ana@medconnect.com  "));
    }

    @Test
    public void asegurarDisponible_noHaceNada_siEmailEsNull() {
        ValidacionEmail.asegurarDisponible(null, email -> {
            throw new AssertionError("no deberia consultar el repositorio si el email es null");
        }, Registro::id, null, () -> new RuntimeException("no deberia lanzar"));
    }

    @Test
    public void asegurarDisponible_noHaceNada_siNadieLoUsa() {
        ValidacionEmail.asegurarDisponible("ana@medconnect.com", email -> Optional.empty(),
                Registro::id, null, () -> new RuntimeException("no deberia lanzar"));
    }

    @Test
    public void asegurarDisponible_lanzaExcepcion_siOtroRegistroYaLoUsa() {
        Registro existente = new Registro(1L);
        assertThrows(RuntimeException.class, () ->
                ValidacionEmail.asegurarDisponible("ana@medconnect.com", email -> Optional.of(existente),
                        Registro::id, 2L, () -> new RuntimeException("ya existe")));
    }

    @Test
    public void asegurarDisponible_noLanza_siElQueLoUsaEsUnoMismo_actualizando() {
        Registro elMismo = new Registro(1L);
        ValidacionEmail.asegurarDisponible("ana@medconnect.com", email -> Optional.of(elMismo),
                Registro::id, 1L, () -> new RuntimeException("no deberia lanzar"));
    }

    @Test
    public void asegurarDisponible_lanzaExcepcion_siEsCreacion_yaAlguienLoUsa() {
        Registro existente = new Registro(1L);
        assertThrows(RuntimeException.class, () ->
                ValidacionEmail.asegurarDisponible("ana@medconnect.com", email -> Optional.of(existente),
                        Registro::id, null, () -> new RuntimeException("ya existe")));
    }
}
