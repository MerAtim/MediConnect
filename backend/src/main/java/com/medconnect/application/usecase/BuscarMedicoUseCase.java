package com.medconnect.application.usecase;

import com.medconnect.domain.model.Medico;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BuscarMedicoUseCase {

    Optional<Medico> buscarPorId(Long id);

    // Para mapear listados (turnos, historias clinicas) sin una query por fila:
    // una sola consulta batch en vez de N llamadas a buscarPorId.
    Map<Long, Medico> buscarPorIds(List<Long> ids);

    Optional<Medico> buscarPorEmail(String email);

    List<Medico> buscarTodos();
}
