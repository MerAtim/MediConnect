package com.medconnect.application.usecase;

import com.medconnect.domain.model.Medico;

import java.util.Optional;

public interface ActualizarMedicoUseCase {

    Optional<Medico> actualizar(Long id, CreateMedicoRequest request);
}
