package com.medconnect.application.usecase;

import com.medconnect.domain.exception.MedicoInvalidoException;
import com.medconnect.domain.port.MedicoRepository;
import com.medconnect.domain.port.TurnoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EliminarMedicoService implements EliminarMedicoUseCase {

    private final MedicoRepository medicoRepository;
    private final TurnoRepository turnoRepository;

    public EliminarMedicoService(MedicoRepository medicoRepository, TurnoRepository turnoRepository) {
        this.medicoRepository = medicoRepository;
        this.turnoRepository = turnoRepository;
    }

    @Override
    public boolean eliminar(Long id) {
        if (medicoRepository.buscarPorId(id).isEmpty()) {
            return false;
        }
        LocalDateTime ahora = LocalDateTime.now();
        boolean tieneTurnosAFuturo = turnoRepository.buscarPorMedico(id).stream()
                .anyMatch(turno -> turno.esFuturoActivo(ahora));
        if (tieneTurnosAFuturo) {
            throw new MedicoInvalidoException(
                    "No se puede eliminar: el medico tiene turnos pendientes o confirmados a futuro. Cancelalos o reasignalos primero.");
        }
        medicoRepository.eliminar(id);
        return true;
    }
}
