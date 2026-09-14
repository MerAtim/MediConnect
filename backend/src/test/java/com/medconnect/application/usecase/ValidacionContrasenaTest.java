package com.medconnect.application.usecase;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ValidacionContrasenaTest {

    @Test
    public void validar_noLanza_siTieneSeisCaracteresOMas() {
        ValidacionContrasena.validar("123456", () -> new RuntimeException("no deberia lanzar"));
        ValidacionContrasena.validar("contrasena-larga", () -> new RuntimeException("no deberia lanzar"));
    }

    @Test
    public void validar_lanzaExcepcion_siTieneMenosDeSeisCaracteres() {
        assertThrows(RuntimeException.class, () ->
                ValidacionContrasena.validar("12345", () -> new RuntimeException("muy corta")));
    }

    @Test
    public void validar_lanzaExcepcion_siEsNull() {
        assertThrows(RuntimeException.class, () ->
                ValidacionContrasena.validar(null, () -> new RuntimeException("obligatoria")));
    }
}
