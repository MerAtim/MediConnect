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

import java.time.LocalDateTime;
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
        // LOW de la re-auditoria e2e (2026-09-08, segunda ronda): no se
        // validaba que la fecha del turno fuera futura. Un turno creado con
        // fecha pasada satisface de inmediato Turno.habilitaHistoriaClinica(),
        // permitiendo escribir historia clinica sobre un turno que nunca
        // representó un compromiso agendado a futuro.
        if (request.getFechaHora().isBefore(LocalDateTime.now())) {
            throw new TurnoInvalidoException("fechaHora debe ser una fecha futura");
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
        boolean solapadoMedico = turnosMedico.stream()
                .anyMatch(t -> t.getFechaHora() != null && t.getFechaHora().equals(request.getFechaHora()));
        if (solapadoMedico) {
            throw new TurnoInvalidoException("El médico no está disponible en la fecha y hora solicitada");
        }

        // MEDIUM de la re-auditoria e2e (2026-09-08): "doble reserva de paciente
        // sin test/decision" -- mismo criterio de deteccion que el chequeo de
        // arriba (igualdad exacta de fechaHora), ahora tambien del lado del
        // paciente para que no termine con dos turnos que se pisan en el mismo
        // horario, aunque sean con medicos distintos.
        List<Turno> turnosPaciente = turnoRepository.buscarPorPaciente(request.getPacienteId());
        boolean solapadoPaciente = turnosPaciente.stream()
                .anyMatch(t -> t.getFechaHora() != null && t.getFechaHora().equals(request.getFechaHora()));
        if (solapadoPaciente) {
            throw new TurnoInvalidoException("El paciente ya tiene otro turno en la fecha y hora solicitada");
        }

        Medico medico = new Medico(request.getMedicoId(), null, null, null, null, null, null);
        Paciente paciente = new Paciente(request.getPacienteId(), null, null, null, null, null, null, null, null);

        Turno turno = new Turno(
                null,
                request.getFechaHora(),
                request.getEspecialidad(),
                medico,
                paciente,
                TurnoEstado.PENDIENTE
        );
        turno.setPreparacion(request.getPreparacion());

        Turno guardado = turnoRepository.guardar(turno);
        return new CreateTurnoResponse(guardado.getId());
    }
}
