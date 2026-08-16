package com.medconnect.domain.port;

import com.medconnect.domain.model.Paciente;
import java.util.List;
import java.util.Optional;

public interface PacienteRepository {

    Paciente guardar(Paciente paciente);

    Optional<Paciente> buscarPorId(Long id);

    List<Paciente> buscarTodos();
}
