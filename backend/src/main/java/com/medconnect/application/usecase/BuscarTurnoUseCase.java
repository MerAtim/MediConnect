package com.medconnect.application.usecase;

import com.medconnect.domain.model.Turno;

import java.util.List;
import java.util.Optional;

public interface BuscarTurnoUseCase {

    Optional<Turno> buscarPorId(Long id);

    List<Turno> buscarPorMedico(Long medicoId);

    List<Turno> buscarPorPaciente(Long pacienteId);

    List<Turno> buscarTodos();

    List<Turno> buscarPaginaPorMedico(Long medicoId, int page, int size);

    long contarPorMedico(Long medicoId);

    List<Turno> buscarPaginaPorPaciente(Long pacienteId, int page, int size);

    long contarPorPaciente(Long pacienteId);

    List<Turno> buscarPagina(int page, int size);

    long contar();
}
