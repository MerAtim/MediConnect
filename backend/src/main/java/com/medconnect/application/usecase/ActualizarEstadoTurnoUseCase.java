package com.medconnect.application.usecase;

import com.medconnect.domain.model.Turno;
import com.medconnect.domain.model.TurnoEstado;

import java.util.Optional;

public interface ActualizarEstadoTurnoUseCase {

    Optional<Turno> actualizarEstado(Long id, TurnoEstado nuevoEstado);
}
