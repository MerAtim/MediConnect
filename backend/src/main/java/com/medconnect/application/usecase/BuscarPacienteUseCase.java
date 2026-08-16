package com.medconnect.application.usecase;

import com.medconnect.domain.model.Paciente;

import java.util.List;
import java.util.Optional;

public interface BuscarPacienteUseCase {

    Optional<Paciente> buscarPorId(Long id);

    List<Paciente> buscarTodos();
}
