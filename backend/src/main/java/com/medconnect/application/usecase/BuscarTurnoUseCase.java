package com.medconnect.application.usecase;

import com.medconnect.domain.model.Turno;

import java.util.List;
import java.util.Optional;

public interface BuscarTurnoUseCase {

    Optional<Turno> buscarPorId(Long id);

    List<Turno> buscarPorMedico(Long medicoId);

    List<Turno> buscarPorPaciente(Long pacienteId);

    List<Turno> buscarTodos();
}
