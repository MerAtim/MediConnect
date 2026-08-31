package com.medconnect.interfaces.rest;

import com.medconnect.application.usecase.ActualizarEstadoTurnoUseCase;
import com.medconnect.application.usecase.BuscarMedicoUseCase;
import com.medconnect.application.usecase.BuscarPacienteUseCase;
import com.medconnect.application.usecase.BuscarTurnoUseCase;
import com.medconnect.application.usecase.CreateTurnoRequest;
import com.medconnect.application.usecase.CreateTurnoResponse;
import com.medconnect.application.usecase.CrearTurnoUseCase;
import com.medconnect.domain.exception.TurnoInvalidoException;
import com.medconnect.domain.model.Medico;
import com.medconnect.domain.model.Paciente;
import com.medconnect.domain.model.Turno;
import com.medconnect.domain.model.TurnoEstado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/turnos")
public class TurnoController {

    private static final Logger log = LoggerFactory.getLogger(TurnoController.class);

    private final CrearTurnoUseCase crearTurnoUseCase;
    private final BuscarTurnoUseCase buscarTurnoUseCase;
    private final ActualizarEstadoTurnoUseCase actualizarEstadoTurnoUseCase;
    private final BuscarMedicoUseCase buscarMedicoUseCase;
    private final BuscarPacienteUseCase buscarPacienteUseCase;

    public TurnoController(CrearTurnoUseCase crearTurnoUseCase, BuscarTurnoUseCase buscarTurnoUseCase,
                            ActualizarEstadoTurnoUseCase actualizarEstadoTurnoUseCase, BuscarMedicoUseCase buscarMedicoUseCase,
                            BuscarPacienteUseCase buscarPacienteUseCase) {
        this.crearTurnoUseCase = crearTurnoUseCase;
        this.buscarTurnoUseCase = buscarTurnoUseCase;
        this.actualizarEstadoTurnoUseCase = actualizarEstadoTurnoUseCase;
        this.buscarMedicoUseCase = buscarMedicoUseCase;
        this.buscarPacienteUseCase = buscarPacienteUseCase;
    }

    @PostMapping
    public ResponseEntity<CreateTurnoResponse> crear(@RequestBody TurnoRequest request) {
        CreateTurnoRequest req = new CreateTurnoRequest(
                request.getFechaHora(),
                request.getEspecialidad(),
                request.getMedicoId(),
                request.getPacienteId(),
                request.getPreparacion()
        );
        CreateTurnoResponse resp = crearTurnoUseCase.crear(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TurnoResponse> buscarPorId(@PathVariable Long id) {
        Optional<Turno> turno = buscarTurnoUseCase.buscarPorId(id);
        if (turno.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!puedeVerTurno(auth, turno.get())) {
            log.warn("Acceso denegado a turno: usuario={} turnoId={}", auth != null ? auth.getName() : null, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(toResponseList(List.of(turno.get())).get(0));
    }

    private boolean puedeVerTurno(Authentication auth, Turno turno) {
        if (tieneRol(auth, "MEDICO")) {
            return buscarMedicoUseCase.buscarPorEmail(auth.getName())
                    .map(medico -> turno.getMedico() != null && medico.getId().equals(turno.getMedico().getId()))
                    .orElse(false);
        }
        if (tieneRol(auth, "PACIENTE")) {
            return buscarPacienteUseCase.buscarPorEmail(auth.getName())
                    .map(paciente -> turno.getPaciente() != null && paciente.getId().equals(turno.getPaciente().getId()))
                    .orElse(false);
        }
        return true;
    }

    @GetMapping
    public ResponseEntity<PageResponse<TurnoResponse>> buscar(
            @RequestParam(required = false) Long medicoId,
            @RequestParam(required = false) Long pacienteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<Turno> turnos;
        if (tieneRol(auth, "MEDICO")) {
            turnos = buscarMedicoUseCase.buscarPorEmail(auth.getName())
                    .map(medico -> buscarTurnoUseCase.buscarPorMedico(medico.getId()))
                    .orElseGet(List::of);
        } else if (tieneRol(auth, "PACIENTE")) {
            turnos = buscarPacienteUseCase.buscarPorEmail(auth.getName())
                    .map(paciente -> buscarTurnoUseCase.buscarPorPaciente(paciente.getId()))
                    .orElseGet(List::of);
        } else if (medicoId != null) {
            turnos = buscarTurnoUseCase.buscarPorMedico(medicoId);
        } else if (pacienteId != null) {
            turnos = buscarTurnoUseCase.buscarPorPaciente(pacienteId);
        } else {
            turnos = buscarTurnoUseCase.buscarTodos();
        }
        return ResponseEntity.ok(PageResponse.of(toResponseList(turnos), page, size));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<TurnoResponse> actualizarEstado(@PathVariable Long id, @RequestBody EstadoTurnoRequest request) {
        TurnoEstado nuevoEstado;
        try {
            nuevoEstado = TurnoEstado.valueOf(request.getEstado());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new TurnoInvalidoException("estado invalido: " + request.getEstado());
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (tieneRol(auth, "PACIENTE")) {
            if (nuevoEstado != TurnoEstado.CANCELADO) {
                throw new TurnoInvalidoException("Un paciente solo puede cancelar su turno");
            }
            Optional<Paciente> paciente = buscarPacienteUseCase.buscarPorEmail(auth.getName());
            Optional<Turno> turno = buscarTurnoUseCase.buscarPorId(id);
            boolean esPropio = paciente.isPresent() && turno.isPresent()
                    && turno.get().getPaciente() != null
                    && paciente.get().getId().equals(turno.get().getPaciente().getId());
            if (!esPropio) {
                log.warn("Cambio de estado de turno denegado: paciente={} turnoId={}", auth.getName(), id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else if (tieneRol(auth, "MEDICO")) {
            Optional<Medico> medico = buscarMedicoUseCase.buscarPorEmail(auth.getName());
            Optional<Turno> turno = buscarTurnoUseCase.buscarPorId(id);
            boolean esPropio = medico.isPresent() && turno.isPresent()
                    && turno.get().getMedico() != null
                    && medico.get().getId().equals(turno.get().getMedico().getId());
            if (!esPropio) {
                log.warn("Cambio de estado de turno denegado: medico={} turnoId={}", auth.getName(), id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        return actualizarEstadoTurnoUseCase.actualizarEstado(id, nuevoEstado)
                .map(turno -> {
                    log.info("Turno actualizado: turnoId={} nuevoEstado={} autor={}",
                            id, nuevoEstado, auth != null ? auth.getName() : null);
                    return ResponseEntity.ok(toResponseList(List.of(turno)).get(0));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private boolean tieneRol(Authentication auth, String rol) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + rol));
    }

    // Batchea las consultas de medico/paciente en 2 queries totales en vez de
    // 2 por turno (evita el N+1 al listar).
    private List<TurnoResponse> toResponseList(List<Turno> turnos) {
        List<Long> medicoIds = turnos.stream()
                .map(t -> t.getMedico() != null ? t.getMedico().getId() : null)
                .filter(Objects::nonNull).distinct().toList();
        List<Long> pacienteIds = turnos.stream()
                .map(t -> t.getPaciente() != null ? t.getPaciente().getId() : null)
                .filter(Objects::nonNull).distinct().toList();
        Map<Long, Medico> medicos = buscarMedicoUseCase.buscarPorIds(medicoIds);
        Map<Long, Paciente> pacientes = buscarPacienteUseCase.buscarPorIds(pacienteIds);
        return turnos.stream().map(t -> toResponse(t, medicos, pacientes)).toList();
    }

    private TurnoResponse toResponse(Turno turno, Map<Long, Medico> medicos, Map<Long, Paciente> pacientes) {
        Long medicoId = turno.getMedico() != null ? turno.getMedico().getId() : null;
        Long pacienteId = turno.getPaciente() != null ? turno.getPaciente().getId() : null;
        Medico medico = medicoId != null ? medicos.get(medicoId) : null;
        Paciente paciente = pacienteId != null ? pacientes.get(pacienteId) : null;
        return new TurnoResponse(
                turno.getId(),
                turno.getFechaHora(),
                turno.getEspecialidad(),
                medicoId,
                medico != null ? medico.getNombre() : null,
                medico != null ? medico.getEspecialidad() : null,
                pacienteId,
                paciente != null ? paciente.getNombre() : null,
                turno.getEstado(),
                turno.getPreparacion()
        );
    }
}
