package com.medconnect.application.usecase;

import com.medconnect.domain.model.Paciente;
import com.medconnect.domain.model.Medico;
import com.medconnect.domain.model.Turno;
import com.medconnect.domain.model.TurnoEstado;
import com.medconnect.domain.port.TurnoRepository;

public class CrearTurnoService implements CrearTurnoUseCase {

    private final TurnoRepository turnoRepository;

    public CrearTurnoService(TurnoRepository turnoRepository) {
        this.turnoRepository = turnoRepository;
    }

    @Override
    public CreateTurnoResponse crear(CreateTurnoRequest request) {
        Medico medico = new Medico(request.getMedicoId(), null, null, null, null, null, null, null);
        Paciente paciente = new Paciente(request.getPacienteId(), null, null, null, null, null, null);

        Turno turno = new Turno(
                null,
                request.getFechaHora(),
                request.getEspecialidad(),
                medico,
                paciente,
                TurnoEstado.PENDIENTE
        );

        Turno guardado = turnoRepository.guardar(turno);
        return new CreateTurnoResponse(guardado.getId());
    }
}
