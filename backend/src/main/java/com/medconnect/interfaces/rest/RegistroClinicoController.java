package com.medconnect.interfaces.rest;

import com.medconnect.application.usecase.BuscarMedicoUseCase;
import com.medconnect.application.usecase.BuscarPacienteUseCase;
import com.medconnect.application.usecase.BuscarRegistroClinicoUseCase;
import com.medconnect.application.usecase.BuscarTurnoUseCase;
import com.medconnect.application.usecase.CreateRegistroClinicoRequest;
import com.medconnect.application.usecase.CreateRegistroClinicoResponse;
import com.medconnect.application.usecase.CrearRegistroClinicoUseCase;
import com.medconnect.domain.model.Medico;
import com.medconnect.domain.model.Paciente;
import com.medconnect.domain.model.RegistroClinico;
import com.medconnect.domain.model.Turno;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/historias-clinicas")
public class RegistroClinicoController {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final CrearRegistroClinicoUseCase crearRegistroClinicoUseCase;
    private final BuscarRegistroClinicoUseCase buscarRegistroClinicoUseCase;
    private final BuscarPacienteUseCase buscarPacienteUseCase;
    private final BuscarMedicoUseCase buscarMedicoUseCase;
    private final BuscarTurnoUseCase buscarTurnoUseCase;

    public RegistroClinicoController(CrearRegistroClinicoUseCase crearRegistroClinicoUseCase,
                                      BuscarRegistroClinicoUseCase buscarRegistroClinicoUseCase,
                                      BuscarPacienteUseCase buscarPacienteUseCase,
                                      BuscarMedicoUseCase buscarMedicoUseCase,
                                      BuscarTurnoUseCase buscarTurnoUseCase) {
        this.crearRegistroClinicoUseCase = crearRegistroClinicoUseCase;
        this.buscarRegistroClinicoUseCase = buscarRegistroClinicoUseCase;
        this.buscarPacienteUseCase = buscarPacienteUseCase;
        this.buscarMedicoUseCase = buscarMedicoUseCase;
        this.buscarTurnoUseCase = buscarTurnoUseCase;
    }

    @PostMapping
    public ResponseEntity<CreateRegistroClinicoResponse> crear(@RequestBody RegistroClinicoRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<Medico> medico = buscarMedicoUseCase.buscarPorEmail(auth.getName());
        if (medico.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        // medicoId se toma siempre de la cuenta autenticada, nunca del body: si viniera del
        // cliente cualquier medico podria escribir un registro atribuido a otro medico.
        CreateRegistroClinicoRequest req = new CreateRegistroClinicoRequest(
                medico.get().getId(),
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!esPacienteDeEseMedico(auth, pacienteId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<RegistroClinico> registros = buscarRegistroClinicoUseCase.buscarPorPaciente(pacienteId);
        return ResponseEntity.ok(toResponseList(registros));
    }

    private boolean esPacienteDeEseMedico(Authentication auth, Long pacienteId) {
        return buscarMedicoUseCase.buscarPorEmail(auth.getName())
                .map(medico -> buscarTurnoUseCase.buscarPorMedico(medico.getId()).stream()
                        .map(Turno::getPaciente)
                        .filter(Objects::nonNull)
                        .map(Paciente::getId)
                        .anyMatch(pacienteId::equals))
                .orElse(false);
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
        Map<Long, Medico> medicos = buscarMedicoUseCase.buscarPorIds(medicoIdsDe(registros));

        StringBuilder sb = new StringBuilder();
        sb.append("Historia clínica\n");
        sb.append("Paciente: ").append(paciente.getNombre()).append(" (DNI ").append(paciente.getDni()).append(")\n");
        sb.append("=".repeat(60)).append("\n\n");
        if (registros.isEmpty()) {
            sb.append("Sin registros clínicos.\n");
        }
        for (RegistroClinico r : registros) {
            Medico medico = medicos.get(r.getMedico().getId());
            String nombreMedico = medico != null ? medico.getNombre() : "médico #" + r.getMedico().getId();
            String especialidad = medico != null && medico.getEspecialidad() != null ? medico.getEspecialidad() : "";
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

    // Batchea la consulta de medicos en 1 query total en vez de 2 por registro
    // (evita el N+1 al listar y al exportar).
    private List<RegistroClinicoResponse> toResponseList(List<RegistroClinico> registros) {
        Map<Long, Medico> medicos = buscarMedicoUseCase.buscarPorIds(medicoIdsDe(registros));
        return registros.stream().map(r -> toResponse(r, medicos)).toList();
    }

    private List<Long> medicoIdsDe(List<RegistroClinico> registros) {
        return registros.stream()
                .map(r -> r.getMedico() != null ? r.getMedico().getId() : null)
                .filter(Objects::nonNull).distinct().toList();
    }

    private RegistroClinicoResponse toResponse(RegistroClinico r, Map<Long, Medico> medicos) {
        Long medicoId = r.getMedico() != null ? r.getMedico().getId() : null;
        Medico medico = medicoId != null ? medicos.get(medicoId) : null;
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
