package com.medconnect.application.usecase;

import com.medconnect.domain.model.Turno;
import com.medconnect.domain.model.TurnoEstado;
import com.medconnect.domain.port.TurnoRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ActualizarEstadoTurnoService implements ActualizarEstadoTurnoUseCase {

    private final TurnoRepository turnoRepository;

    public ActualizarEstadoTurnoService(TurnoRepository turnoRepository) {
        this.turnoRepository = turnoRepository;
    }

    @Override
    public Optional<Turno> actualizarEstado(Long id, TurnoEstado nuevoEstado) {
        Optional<Turno> turnoOpt = turnoRepository.buscarPorId(id);
        if (turnoOpt.isEmpty()) {
            return Optional.empty();
        }

        Turno turno = turnoOpt.get();
        turno.cambiarEstado(nuevoEstado);
        return Optional.of(turnoRepository.guardar(turno));
    }
}
