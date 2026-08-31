package com.medconnect.application.usecase;

import com.medconnect.domain.model.Paciente;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BuscarPacienteUseCase {

    Optional<Paciente> buscarPorId(Long id);

    // Para mapear listados (turnos, historias clinicas, pacientes de un medico)
    // sin una query por fila: una sola consulta batch en vez de N buscarPorId.
    Map<Long, Paciente> buscarPorIds(List<Long> ids);

    Optional<Paciente> buscarPorEmail(String email);

    List<Paciente> buscarTodos();
}
