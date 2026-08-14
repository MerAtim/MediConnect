package com.medconnect.domain.port;

import com.medconnect.domain.model.Turno;
import java.util.List;
import java.util.Optional;

public interface TurnoRepository {

    Turno guardar(Turno turno);

    Optional<Turno> buscarPorId(Long id);

    List<Turno> buscarPorMedico(Long medicoId);

    List<Turno> buscarPorPaciente(Long pacienteId);

    List<Turno> buscarTodos();
}