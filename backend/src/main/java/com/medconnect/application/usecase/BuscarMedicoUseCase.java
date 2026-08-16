package com.medconnect.application.usecase;

import com.medconnect.domain.model.Medico;

import java.util.List;
import java.util.Optional;

public interface BuscarMedicoUseCase {

    Optional<Medico> buscarPorId(Long id);

    List<Medico> buscarTodos();
}
