package com.medconnect.application.usecase;

import com.medconnect.domain.exception.PacienteInvalidoException;
import com.medconnect.domain.port.PacienteRepository;
import com.medconnect.domain.port.TurnoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EliminarPacienteService implements EliminarPacienteUseCase {

    private final PacienteRepository pacienteRepository;
    private final TurnoRepository turnoRepository;

    public EliminarPacienteService(PacienteRepository pacienteRepository, TurnoRepository turnoRepository) {
        this.pacienteRepository = pacienteRepository;
        this.turnoRepository = turnoRepository;
    }

    @Override
    public boolean eliminar(Long id) {
        if (pacienteRepository.buscarPorId(id).isEmpty()) {
            return false;
        }
        LocalDateTime ahora = LocalDateTime.now();
        boolean tieneTurnosAFuturo = turnoRepository.buscarPorPaciente(id).stream()
                .anyMatch(turno -> turno.esFuturoActivo(ahora));
        if (tieneTurnosAFuturo) {
            throw new PacienteInvalidoException(
                    "No se puede eliminar: el paciente tiene turnos pendientes o confirmados a futuro. Cancelalos o reasignalos primero.");
        }
        pacienteRepository.eliminar(id);
        return true;
    }
}
