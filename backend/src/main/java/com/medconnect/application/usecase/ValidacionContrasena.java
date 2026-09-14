package com.medconnect.application.usecase;

import java.util.function.Supplier;

// LOW de la re-auditoria e2e (2026-09-08, segunda ronda): la regla
// "contrasena minimo 6 caracteres" estaba duplicada literalmente en
// RegistrarUsuarioService.guardar() y ActualizarContrasenaService.validarNueva()
// sin una unica fuente de verdad -- mismo patron de duplicacion que
// ValidacionEmail. Package-private: es un detalle de implementacion de los
// use cases de este paquete, no una API publica.
final class ValidacionContrasena {

    private static final int LONGITUD_MINIMA = 6;

    private ValidacionContrasena() {
    }

    static void validar(String contrasena, Supplier<? extends RuntimeException> siInvalida) {
        if (contrasena == null || contrasena.length() < LONGITUD_MINIMA) {
            throw siInvalida.get();
        }
    }
}
