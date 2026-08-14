package com.medconnect.interfaces.rest;

import com.medconnect.application.usecase.CreateTurnoRequest;
import com.medconnect.application.usecase.CreateTurnoResponse;
import com.medconnect.application.usecase.CrearTurnoUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/turnos")
@CrossOrigin(origins = "*")
public class TurnoController {

    private final CrearTurnoUseCase crearTurnoUseCase;

    public TurnoController(CrearTurnoUseCase crearTurnoUseCase) {
        this.crearTurnoUseCase = crearTurnoUseCase;
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
}
