package com.medconnect.interfaces.rest;

import com.medconnect.application.usecase.BuscarPacienteUseCase;
import com.medconnect.application.usecase.CreatePacienteRequest;
import com.medconnect.application.usecase.CreatePacienteResponse;
import com.medconnect.application.usecase.CrearPacienteUseCase;
import com.medconnect.domain.model.Paciente;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
@CrossOrigin(origins = "*")
public class PacienteController {

    private final CrearPacienteUseCase crearPacienteUseCase;
    private final BuscarPacienteUseCase buscarPacienteUseCase;

    public PacienteController(CrearPacienteUseCase crearPacienteUseCase, BuscarPacienteUseCase buscarPacienteUseCase) {
        this.crearPacienteUseCase = crearPacienteUseCase;
        this.buscarPacienteUseCase = buscarPacienteUseCase;
    }

    @PostMapping
    public ResponseEntity<CreatePacienteResponse> crear(@RequestBody PacienteRequest request) {
        CreatePacienteRequest req = new CreatePacienteRequest(
                request.getNombre(),
                request.getDni(),
                request.getTelefono(),
                request.getDireccion(),
                request.getObraSocial(),
                request.getNumeroAfiliado(),
                request.getPlan(),
                request.getEmail()
        );
        CreatePacienteResponse resp = crearPacienteUseCase.crear(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponse> buscarPorId(@PathVariable Long id) {
        return buscarPacienteUseCase.buscarPorId(id)
                .map(paciente -> ResponseEntity.ok(toResponse(paciente)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<PacienteResponse>> buscarTodos() {
        List<Paciente> pacientes = buscarPacienteUseCase.buscarTodos();
        return ResponseEntity.ok(pacientes.stream().map(this::toResponse).toList());
    }

    private PacienteResponse toResponse(Paciente paciente) {
        return new PacienteResponse(
                paciente.getId(),
                paciente.getNombre(),
                paciente.getDni(),
                paciente.getTelefono(),
                paciente.getDireccion(),
                paciente.getObraSocial(),
                paciente.getNumeroAfiliado(),
                paciente.getPlan(),
                paciente.getEmail()
        );
    }
}
