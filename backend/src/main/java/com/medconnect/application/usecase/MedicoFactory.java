package com.medconnect.application.usecase;

import com.medconnect.domain.exception.MedicoInvalidoException;
import com.medconnect.domain.model.Medico;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

// Antes duplicado entre CrearMedicoService y ActualizarMedicoService (la
// unica diferencia real era el id: null al crear, el existente al
// actualizar). Package-private: detalle de implementacion de los use cases
// de este paquete, no una API publica.
final class MedicoFactory {

    private MedicoFactory() {
    }

    static Medico crear(Long id, CreateMedicoRequest request, Function<String, Optional<Medico>> buscarPorEmail,
                         Predicate<String> existeEmailEnPerfilEliminado) {
        request.validar();

        String email = ValidacionEmail.normalizarOpcional(request.getEmail(),
                () -> new MedicoInvalidoException("email invalido"));
        ValidacionEmail.asegurarDisponible(email, buscarPorEmail, Medico::getId, id,
                () -> new MedicoInvalidoException("ya existe un medico con ese email"));
        ValidacionEmail.asegurarNoPerteneceAPerfilEliminado(email, existeEmailEnPerfilEliminado,
                () -> new MedicoInvalidoException("ese email pertenece a un perfil eliminado, no se puede reutilizar"));

        return new Medico(
                id,
                request.getNombre(),
                request.getEspecialidad(),
                request.getMatricula(),
                request.getDireccion(),
                request.getTelefono(),
                email);
    }
}
