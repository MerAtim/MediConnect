package com.medconnect.application.usecase;

import com.medconnect.domain.exception.TurnoInvalidoException;
import com.medconnect.domain.model.Paciente;
import com.medconnect.domain.model.Medico;
import com.medconnect.domain.model.Turno;
import com.medconnect.domain.model.TurnoEstado;
import com.medconnect.domain.port.MedicoRepository;
import com.medconnect.domain.port.PacienteRepository;
import com.medconnect.domain.port.TurnoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CrearTurnoService implements CrearTurnoUseCase {

    private final TurnoRepository turnoRepository;
    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;

    public CrearTurnoService(TurnoRepository turnoRepository, MedicoRepository medicoRepository, PacienteRepository pacienteRepository) {
        this.turnoRepository = turnoRepository;
        this.medicoRepository = medicoRepository;
        this.pacienteRepository = pacienteRepository;
    }

    @Override
    public CreateTurnoResponse crear(CreateTurnoRequest request) {
        // Validaciones básicas
        if (request.getFechaHora() == null) {
            throw new TurnoInvalidoException("fechaHora es obligatoria");
        }
        if (request.getMedicoId() == null) {
            throw new TurnoInvalidoException("medicoId es obligatorio");
        }
        if (request.getPacienteId() == null) {
            throw new TurnoInvalidoException("pacienteId es obligatorio");
        }
        if (request.getEspecialidad() == null || request.getEspecialidad().trim().isEmpty()) {
            throw new TurnoInvalidoException("especialidad es obligatoria");
        }
        if (medicoRepository.buscarPorId(request.getMedicoId()).isEmpty()) {
            throw new TurnoInvalidoException("El médico indicado no existe");
        }
        if (pacienteRepository.buscarPorId(request.getPacienteId()).isEmpty()) {
            throw new TurnoInvalidoException("El paciente indicado no existe");
        }

        // Verificar solapamiento: mismo médico y misma fechaHora
        List<Turno> turnosMedico = turnoRepository.buscarPorMedico(request.getMedicoId());
        boolean solapado = turnosMedico.stream()
                .anyMatch(t -> t.getFechaHora() != null && t.getFechaHora().equals(request.getFechaHora()));
        if (solapado) {
            throw new TurnoInvalidoException("El médico no está disponible en la fecha y hora solicitada");
        }

        Medico medico = new Medico(request.getMedicoId(), null, null, null, null, null, null, null);
        Paciente paciente = new Paciente(request.getPacienteId(), null, null, null, null, null, null, null, null);

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
