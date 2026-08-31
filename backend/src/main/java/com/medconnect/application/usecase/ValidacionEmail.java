package com.medconnect.application.usecase;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

// Antes reimplementado con variaciones en Crear/Actualizar Medico/Paciente y
// en RegistrarUsuarioService (5 copias). Package-private: es un detalle de
// implementacion de los use cases de este paquete, no una API publica.
final class ValidacionEmail {

    private ValidacionEmail() {
    }

    static String normalizar(String email) {
        return (email == null || email.trim().isEmpty()) ? null : email.trim();
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
}
