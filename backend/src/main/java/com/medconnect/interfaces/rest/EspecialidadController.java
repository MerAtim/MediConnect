package com.medconnect.interfaces.rest;

import com.medconnect.application.usecase.EspecialidadDuracion;
import com.medconnect.application.usecase.GestionarDuracionEspecialidadUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/especialidades")
public class EspecialidadController {

    private final GestionarDuracionEspecialidadUseCase gestionarDuracionEspecialidadUseCase;

    public EspecialidadController(GestionarDuracionEspecialidadUseCase gestionarDuracionEspecialidadUseCase) {
        this.gestionarDuracionEspecialidadUseCase = gestionarDuracionEspecialidadUseCase;
    }

    @GetMapping("/duraciones")
    public ResponseEntity<List<EspecialidadDuracionResponse>> listarDuraciones() {
        List<EspecialidadDuracionResponse> duraciones = gestionarDuracionEspecialidadUseCase.listarTodas().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(duraciones);
    }

    @PutMapping("/{especialidad}/duracion")
    public ResponseEntity<Void> establecerDuracion(@PathVariable String especialidad, @RequestBody EspecialidadDuracionRequest request) {
        gestionarDuracionEspecialidadUseCase.establecer(especialidad, request.getDuracionMinutos());
        return ResponseEntity.ok().build();
    }

    private EspecialidadDuracionResponse toResponse(EspecialidadDuracion duracion) {
        return new EspecialidadDuracionResponse(duracion.getEspecialidad(), duracion.getDuracionMinutos());
    }
}
