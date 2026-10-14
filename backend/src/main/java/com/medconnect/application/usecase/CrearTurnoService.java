package com.medconnect.application.usecase;

import com.medconnect.domain.exception.TurnoInvalidoException;
import com.medconnect.domain.model.Paciente;
import com.medconnect.domain.model.Medico;
import com.medconnect.domain.model.Turno;
import com.medconnect.domain.model.TurnoEstado;
import com.medconnect.domain.port.EspecialidadDuracionRepository;
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
    private final EspecialidadDuracionRepository especialidadDuracionRepository;

    public CrearTurnoService(TurnoRepository turnoRepository, MedicoRepository medicoRepository,
                              PacienteRepository pacienteRepository,
                              EspecialidadDuracionRepository especialidadDuracionRepository) {
        this.turnoRepository = turnoRepository;
        this.medicoRepository = medicoRepository;
        this.pacienteRepository = pacienteRepository;
        this.especialidadDuracionRepository = especialidadDuracionRepository;
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
        Medico medico = medicoRepository.buscarPorId(request.getMedicoId())
                .orElseThrow(() -> new TurnoInvalidoException("El médico indicado no existe"));
        if (pacienteRepository.buscarPorId(request.getPacienteId()).isEmpty()) {
            throw new TurnoInvalidoException("El paciente indicado no existe");
        }
        // Integridad general (no solo para el flujo de autogestion del
        // paciente que se agrega en un PR siguiente): sin esto se podia crear
        // un turno de una especialidad que el medico elegido no ejerce.
        if (medico.getEspecialidad() != null && !medico.getEspecialidad().equals(request.getEspecialidad())) {
            throw new TurnoInvalidoException("La especialidad indicada no coincide con la del médico");
        }

        LocalDateTime nuevoInicio = request.getFechaHora();
        LocalDateTime nuevoFin = nuevoInicio.plusMinutes(duracionDe(request.getEspecialidad()));

        // Antes el solapamiento se detectaba solo por igualdad exacta de
        // fechaHora, lo que no alcanza ahora que el paciente puede reservar
        // su propio turno sin un humano agendando con sentido comun -- dos
        // turnos de 30 minutos a las 10:00 y 10:15 pasaban sin problema.
        // Se compara contra un intervalo semiabierto [inicio, fin): el
        // limite exacto (un turno termina justo cuando el otro empieza) no
        // cuenta como solapamiento. Se excluyen los turnos CANCELADO -- bug
        // encontrado al implementar esto: antes contaban igual porque
        // buscarPorMedico/buscarPorPaciente no filtran por estado.
        List<Turno> turnosMedico = turnoRepository.buscarPorMedico(request.getMedicoId());
        boolean solapadoMedico = turnosMedico.stream()
                .filter(t -> t.getEstado() != TurnoEstado.CANCELADO)
                .anyMatch(t -> seSuperponen(nuevoInicio, nuevoFin, t));
        if (solapadoMedico) {
            throw new TurnoInvalidoException("El médico no está disponible en la fecha y hora solicitada");
        }

        // MEDIUM de la re-auditoria e2e (2026-09-08): "doble reserva de paciente
        // sin test/decision" -- mismo criterio de deteccion que el chequeo de
        // arriba, ahora tambien del lado del paciente para que no termine con
        // dos turnos que se pisan en el mismo horario, aunque sean con
        // medicos distintos.
        List<Turno> turnosPaciente = turnoRepository.buscarPorPaciente(request.getPacienteId());
        boolean solapadoPaciente = turnosPaciente.stream()
                .filter(t -> t.getEstado() != TurnoEstado.CANCELADO)
                .anyMatch(t -> seSuperponen(nuevoInicio, nuevoFin, t));
        if (solapadoPaciente) {
            throw new TurnoInvalidoException("El paciente ya tiene otro turno en la fecha y hora solicitada");
        }

        Medico medicoRef = Medico.conId(request.getMedicoId());
        Paciente paciente = Paciente.conId(request.getPacienteId());

        Turno turno = new Turno(
                null,
                request.getFechaHora(),
                request.getEspecialidad(),
                medicoRef,
                paciente,
                TurnoEstado.PENDIENTE
        );
        turno.setPreparacion(request.getPreparacion());

        Turno guardado = turnoRepository.guardar(turno);
        return new CreateTurnoResponse(guardado.getId());
    }

    private int duracionDe(String especialidad) {
        return especialidadDuracionRepository.buscarDuracion(especialidad);
    }

    // La duracion no se persiste por turno (ver EspecialidadDuracionRepository):
    // se resuelve con la duracion VIGENTE de la especialidad de cada turno
    // existente, no la que tenia cuando se creo. Efecto retroactivo
    // aceptado como limitacion conocida de v1.
    private boolean seSuperponen(LocalDateTime nuevoInicio, LocalDateTime nuevoFin, Turno existente) {
        LocalDateTime existenteInicio = existente.getFechaHora();
        if (existenteInicio == null) {
            return false;
        }
        LocalDateTime existenteFin = existenteInicio.plusMinutes(duracionDe(existente.getEspecialidad()));
        return nuevoInicio.isBefore(existenteFin) && existenteInicio.isBefore(nuevoFin);
    }
}
