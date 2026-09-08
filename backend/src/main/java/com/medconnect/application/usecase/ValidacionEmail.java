package com.medconnect.application.usecase;

import com.medconnect.domain.model.Email;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

// Antes reimplementado con variaciones en Crear/Actualizar Medico/Paciente y
// en RegistrarUsuarioService (5 copias). Package-private: es un detalle de
// implementacion de los use cases de este paquete, no una API publica.
final class ValidacionEmail {

    private ValidacionEmail() {
    }

    // Devuelve el email trimeado y validado, o null si vino vacio (Medico/
    // Paciente pueden no tener cuenta vinculada). El formato lo valida Email
    // (Value Object en domain.model) -- este metodo solo lo traduce a la
    // excepcion de negocio que corresponda en cada caso de uso.
    static String normalizarOpcional(String email, Supplier<? extends RuntimeException> siFormatoInvalido) {
        String trimmed = (email == null) ? null : email.trim();
        if (trimmed == null || trimmed.isEmpty()) {
            return null;
        }
        if (!Email.esFormatoValido(trimmed)) {
            throw siFormatoInvalido.get();
        }
        return trimmed;
    }

    static <T> void asegurarDisponible(String email, Function<String, Optional<T>> buscarPorEmail,
                                        Function<T, Long> idDe, Long idPropio,
                                        Supplier<? extends RuntimeException> siYaExiste) {
        if (email == null) {
            return;
        }
        buscarPorEmail.apply(email).ifPresent(existente -> {
            if (idPropio == null || !idPropio.equals(idDe.apply(existente))) {
                throw siYaExiste.get();
            }
        });
    }

    // MEDIUM de la re-auditoria e2e (2026-09-08): el UNIQUE(email) de la
    // base es a nivel de toda la tabla (incluye filas soft-deleted).
    // asegurarDisponible (arriba) solo mira perfiles activos, asi que sin
    // este chequeo aparte un email "disponible" para la app podia
    // pertenecer a un perfil eliminado y explotar en la unique constraint
    // (500 crudo) al guardar.
    static void asegurarNoPerteneceAPerfilEliminado(String email, Predicate<String> existeEnPerfilEliminado,
                                                      Supplier<? extends RuntimeException> siPerteneceAEliminado) {
        if (email == null) {
            return;
        }
        if (existeEnPerfilEliminado.test(email)) {
            throw siPerteneceAEliminado.get();
        }
    }
}
