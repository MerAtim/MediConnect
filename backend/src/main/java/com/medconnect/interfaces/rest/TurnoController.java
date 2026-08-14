package com.medconnect.interfaces.rest;

import com.medconnect.application.usecase.BuscarTurnoUseCase;
import com.medconnect.application.usecase.CreateTurnoRequest;
import com.medconnect.application.usecase.CreateTurnoResponse;
import com.medconnect.application.usecase.CrearTurnoUseCase;
import com.medconnect.domain.model.Turno;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/turnos")
@CrossOrigin(origins = "*")
public class TurnoController {

    private final CrearTurnoUseCase crearTurnoUseCase;
    private final BuscarTurnoUseCase buscarTurnoUseCase;

    public TurnoController(CrearTurnoUseCase crearTurnoUseCase, BuscarTurnoUseCase buscarTurnoUseCase) {
        this.crearTurnoUseCase = crearTurnoUseCase;
        this.buscarTurnoUseCase = buscarTurnoUseCase;
    }

    @PostMapping
    public ResponseEntity<CreateTurnoResponse> crear(@RequestBody TurnoRequest request) {
        CreateTurnoRequest req = new CreateTurnoRequest(
                request.getFechaHora(),
                request.getEspecialidad(),
                request.getMedicoId(),
                request.getPacienteId()
        );
        CreateTurnoResponse resp = crearTurnoUseCase.crear(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TurnoResponse> buscarPorId(@PathVariable Long id) {
        return buscarTurnoUseCase.buscarPorId(id)
                .map(turno -> ResponseEntity.ok(toResponse(turno)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<TurnoResponse>> buscar(
            @RequestParam(required = false) Long medicoId,
            @RequestParam(required = false) Long pacienteId) {
        List<Turno> turnos;
        if (medicoId != null) {
            turnos = buscarTurnoUseCase.buscarPorMedico(medicoId);
        } else if (pacienteId != null) {
            turnos = buscarTurnoUseCase.buscarPorPaciente(pacienteId);
        } else {
            turnos = buscarTurnoUseCase.buscarTodos();
        }
        return ResponseEntity.ok(turnos.stream().map(this::toResponse).toList());
    }

    private TurnoResponse toResponse(Turno turno) {
        return new TurnoResponse(
                turno.getId(),
                turno.getFechaHora(),
                turno.getEspecialidad(),
                turno.getMedico() != null ? turno.getMedico().getId() : null,
                turno.getPaciente() != null ? turno.getPaciente().getId() : null,
                turno.getEstado()
        );
    }
}
