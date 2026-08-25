package com.medconnect.interfaces.rest;

import com.medconnect.application.usecase.BuscarMedicoUseCase;
import com.medconnect.application.usecase.BuscarPacienteUseCase;
import com.medconnect.application.usecase.BuscarRegistroClinicoUseCase;
import com.medconnect.application.usecase.CreateRegistroClinicoRequest;
import com.medconnect.application.usecase.CreateRegistroClinicoResponse;
import com.medconnect.application.usecase.CrearRegistroClinicoUseCase;
import com.medconnect.domain.model.Medico;
import com.medconnect.domain.model.Paciente;
import com.medconnect.domain.model.RegistroClinico;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/historias-clinicas")
public class RegistroClinicoController {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final CrearRegistroClinicoUseCase crearRegistroClinicoUseCase;
    private final BuscarRegistroClinicoUseCase buscarRegistroClinicoUseCase;
    private final BuscarPacienteUseCase buscarPacienteUseCase;
    private final BuscarMedicoUseCase buscarMedicoUseCase;

    public RegistroClinicoController(CrearRegistroClinicoUseCase crearRegistroClinicoUseCase,
                                      BuscarRegistroClinicoUseCase buscarRegistroClinicoUseCase,
                                      BuscarPacienteUseCase buscarPacienteUseCase,
                                      BuscarMedicoUseCase buscarMedicoUseCase) {
        this.crearRegistroClinicoUseCase = crearRegistroClinicoUseCase;
        this.buscarRegistroClinicoUseCase = buscarRegistroClinicoUseCase;
        this.buscarPacienteUseCase = buscarPacienteUseCase;
        this.buscarMedicoUseCase = buscarMedicoUseCase;
    }

    @PostMapping
    public ResponseEntity<CreateRegistroClinicoResponse> crear(@RequestBody RegistroClinicoRequest request) {
        CreateRegistroClinicoRequest req = new CreateRegistroClinicoRequest(
                request.getMedicoId(),
                request.getPacienteId(),
                request.getDiagnostico(),
                request.getTratamiento(),
                request.getObservaciones()
        );
        CreateRegistroClinicoResponse resp = crearRegistroClinicoUseCase.crear(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping
    public ResponseEntity<List<RegistroClinicoResponse>> buscarPorPaciente(@RequestParam Long pacienteId) {
        List<RegistroClinico> registros = buscarRegistroClinicoUseCase.buscarPorPaciente(pacienteId);
        return ResponseEntity.ok(registros.stream().map(this::toResponse).toList());
    }

    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportar(@RequestParam Long pacienteId) {
        return buscarPacienteUseCase.buscarPorId(pacienteId)
                .map(paciente -> {
                    List<RegistroClinico> registros = buscarRegistroClinicoUseCase.buscarPorPaciente(pacienteId);
                    byte[] contenido = formatearDocumento(paciente, registros).getBytes(StandardCharsets.UTF_8);
                    return ResponseEntity.ok()
                            .contentType(MediaType.TEXT_PLAIN)
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                    ContentDisposition.attachment()
                                            .filename("historia-clinica-paciente-" + pacienteId + ".txt", StandardCharsets.UTF_8)
                                            .build().toString())
                            .body(contenido);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private String formatearDocumento(Paciente paciente, List<RegistroClinico> registros) {
        StringBuilder sb = new StringBuilder();
        sb.append("Historia clínica\n");
        sb.append("Paciente: ").append(paciente.getNombre()).append(" (DNI ").append(paciente.getDni()).append(")\n");
        sb.append("=".repeat(60)).append("\n\n");
        if (registros.isEmpty()) {
            sb.append("Sin registros clínicos.\n");
        }
        for (RegistroClinico r : registros) {
            String nombreMedico = buscarMedicoUseCase.buscarPorId(r.getMedico().getId())
                    .map(Medico::getNombre).orElse("médico #" + r.getMedico().getId());
            String especialidad = buscarMedicoUseCase.buscarPorId(r.getMedico().getId())
                    .map(Medico::getEspecialidad).orElse("");
            sb.append(r.getFecha().format(FORMATO_FECHA)).append(" — ").append(nombreMedico);
            if (!especialidad.isBlank()) sb.append(" (").append(especialidad).append(")");
            sb.append("\n");
            sb.append("  Diagnóstico: ").append(r.getDiagnostico()).append("\n");
            sb.append("  Tratamiento: ").append(r.getTratamiento()).append("\n");
            if (r.getObservaciones() != null && !r.getObservaciones().isBlank()) {
                sb.append("  Observaciones: ").append(r.getObservaciones()).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private RegistroClinicoResponse toResponse(RegistroClinico r) {
        Long medicoId = r.getMedico() != null ? r.getMedico().getId() : null;
        Medico medico = medicoId != null ? buscarMedicoUseCase.buscarPorId(medicoId).orElse(null) : null;
        return new RegistroClinicoResponse(
                r.getId(),
                r.getFecha(),
                medicoId,
                medico != null ? medico.getNombre() : null,
                medico != null ? medico.getEspecialidad() : null,
                r.getPaciente() != null ? r.getPaciente().getId() : null,
                r.getDiagnostico(),
                r.getTratamiento(),
                r.getObservaciones()
        );
    }
}
