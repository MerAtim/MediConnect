package com.medconnect.application.usecase;

import com.medconnect.domain.model.Paciente;

import java.util.Optional;

public interface ActualizarPacienteUseCase {

    Optional<Paciente> actualizar(Long id, CreatePacienteRequest request);
}
