package com.medconnect.interfaces.rest;

import com.medconnect.application.usecase.ActualizarPacienteUseCase;
import com.medconnect.application.usecase.BuscarMedicoUseCase;
import com.medconnect.application.usecase.BuscarPacienteUseCase;
import com.medconnect.application.usecase.BuscarTurnoUseCase;
import com.medconnect.application.usecase.CreatePacienteRequest;
import com.medconnect.application.usecase.CreatePacienteResponse;
import com.medconnect.application.usecase.CrearPacienteUseCase;
import com.medconnect.application.usecase.EliminarPacienteUseCase;
import com.medconnect.domain.model.Medico;
import com.medconnect.domain.model.Paciente;
import com.medconnect.domain.model.Turno;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/pacientes")
@CrossOrigin(origins = "*")
public class PacienteController {

    private final CrearPacienteUseCase crearPacienteUseCase;
    private final BuscarPacienteUseCase buscarPacienteUseCase;
    private final ActualizarPacienteUseCase actualizarPacienteUseCase;
    private final EliminarPacienteUseCase eliminarPacienteUseCase;
    private final BuscarMedicoUseCase buscarMedicoUseCase;
    private final BuscarTurnoUseCase buscarTurnoUseCase;

    public PacienteController(CrearPacienteUseCase crearPacienteUseCase, BuscarPacienteUseCase buscarPacienteUseCase,
                               ActualizarPacienteUseCase actualizarPacienteUseCase, EliminarPacienteUseCase eliminarPacienteUseCase,
                               BuscarMedicoUseCase buscarMedicoUseCase, BuscarTurnoUseCase buscarTurnoUseCase) {
        this.crearPacienteUseCase = crearPacienteUseCase;
        this.buscarPacienteUseCase = buscarPacienteUseCase;
        this.actualizarPacienteUseCase = actualizarPacienteUseCase;
        this.eliminarPacienteUseCase = eliminarPacienteUseCase;
        this.buscarMedicoUseCase = buscarMedicoUseCase;
        this.buscarTurnoUseCase = buscarTurnoUseCase;
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

    @GetMapping("/me")
    public ResponseEntity<PacienteResponse> obtenerPropio() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return buscarPacienteUseCase.buscarPorEmail(auth.getName())
                .map(paciente -> ResponseEntity.ok(toResponse(paciente)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponse> buscarPorId(@PathVariable Long id) {
        Optional<Paciente> paciente = buscarPacienteUseCase.buscarPorId(id);
        if (paciente.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (tieneRolMedico(auth) && !esPacienteDeEseMedico(auth, id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(toResponse(paciente.get()));
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

    @GetMapping("/buscar-por-dni")
    public ResponseEntity<PacienteResponse> buscarPorDni(@RequestParam String dni) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return pacientesVisibles(auth).stream()
                .filter(p -> dni.equals(p.getDni()))
                .findFirst()
                .map(p -> ResponseEntity.ok(toResponse(p)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/emails-vinculados")
    public ResponseEntity<List<EmailVinculadoResponse>> emailsVinculados() {
        List<EmailVinculadoResponse> emails = buscarPacienteUseCase.buscarTodos().stream()
                .filter(p -> p.getEmail() != null && !p.getEmail().isBlank())
                .map(p -> new EmailVinculadoResponse(p.getId(), p.getEmail()))
                .toList();
        return ResponseEntity.ok(emails);
    }

    @GetMapping
    public ResponseEntity<PageResponse<PacienteResponse>> buscarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<PacienteResponse> todos = pacientesVisibles(auth).stream().map(this::toResponse).toList();
        return ResponseEntity.ok(PageResponse.of(todos, page, size));
    }

    private List<Paciente> pacientesVisibles(Authentication auth) {
        if (tieneRolMedico(auth)) {
            return buscarMedicoUseCase.buscarPorEmail(auth.getName())
                    .map(this::pacientesDeEseMedico)
                    .orElseGet(List::of);
        }
        return buscarPacienteUseCase.buscarTodos();
    }

    private List<Paciente> pacientesDeEseMedico(Medico medico) {
        return buscarTurnoUseCase.buscarPorMedico(medico.getId()).stream()
                .map(Turno::getPaciente)
                .filter(Objects::nonNull)
                .map(Paciente::getId)
                .distinct()
                .map(buscarPacienteUseCase::buscarPorId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private boolean tieneRolMedico(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MEDICO"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PacienteResponse> actualizar(@PathVariable Long id, @RequestBody PacienteRequest request) {
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
        return actualizarPacienteUseCase.actualizar(id, req)
                .map(paciente -> ResponseEntity.ok(toResponse(paciente)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        boolean eliminado = eliminarPacienteUseCase.eliminar(id);
        return eliminado ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
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
