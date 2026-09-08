package com.medconnect.application.usecase;

import com.medconnect.domain.exception.PacienteInvalidoException;
import com.medconnect.domain.model.Paciente;

import java.util.Optional;
import java.util.function.Function;

// Antes duplicado entre CrearPacienteService y ActualizarPacienteService (la
// unica diferencia real era el id: null al crear, el existente al
// actualizar). Package-private: detalle de implementacion de los use cases
// de este paquete, no una API publica.
final class PacienteFactory {

    private PacienteFactory() {
    }

    static Paciente crear(Long id, CreatePacienteRequest request, Function<String, Optional<Paciente>> buscarPorEmail) {
        request.validar();

        String email = ValidacionEmail.normalizarOpcional(request.getEmail(),
                () -> new PacienteInvalidoException("email invalido"));
        ValidacionEmail.asegurarDisponible(email, buscarPorEmail, Paciente::getId, id,
                () -> new PacienteInvalidoException("ya existe un paciente con ese email"));

        return new Paciente(
                id,
                request.getNombre(),
                request.getDni(),
                request.getTelefono(),
                request.getDireccion(),
                request.getObraSocial(),
                request.getNumeroAfiliado(),
                request.getPlan(),
                email
        );
    }
}
