package com.medconnect.interfaces.rest;

import com.medconnect.application.usecase.ActualizarMedicoUseCase;
import com.medconnect.application.usecase.BuscarMedicoUseCase;
import com.medconnect.application.usecase.CreateMedicoRequest;
import com.medconnect.application.usecase.CreateMedicoResponse;
import com.medconnect.application.usecase.CrearMedicoUseCase;
import com.medconnect.application.usecase.EliminarMedicoUseCase;
import com.medconnect.domain.model.Medico;
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

@RestController
@RequestMapping("/api/medicos")
@CrossOrigin(origins = "*")
public class MedicoController {

    private final CrearMedicoUseCase crearMedicoUseCase;
    private final BuscarMedicoUseCase buscarMedicoUseCase;
    private final ActualizarMedicoUseCase actualizarMedicoUseCase;
    private final EliminarMedicoUseCase eliminarMedicoUseCase;

    public MedicoController(CrearMedicoUseCase crearMedicoUseCase, BuscarMedicoUseCase buscarMedicoUseCase,
                             ActualizarMedicoUseCase actualizarMedicoUseCase, EliminarMedicoUseCase eliminarMedicoUseCase) {
        this.crearMedicoUseCase = crearMedicoUseCase;
        this.buscarMedicoUseCase = buscarMedicoUseCase;
        this.actualizarMedicoUseCase = actualizarMedicoUseCase;
        this.eliminarMedicoUseCase = eliminarMedicoUseCase;
    }

    @PostMapping
    public ResponseEntity<CreateMedicoResponse> crear(@RequestBody MedicoRequest request) {
        CreateMedicoRequest req = new CreateMedicoRequest(
                request.getNombre(),
                request.getEspecialidad(),
                request.getMatricula(),
                request.getDireccion(),
                request.getTelefono(),
                request.getEmail()
        );
        CreateMedicoResponse resp = crearMedicoUseCase.crear(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/me")
    public ResponseEntity<MedicoResponse> obtenerPropio() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return buscarMedicoUseCase.buscarPorEmail(auth.getName())
                .map(medico -> ResponseEntity.ok(toResponse(medico)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicoResponse> buscarPorId(@PathVariable Long id) {
        return buscarMedicoUseCase.buscarPorId(id)
                .map(medico -> ResponseEntity.ok(toResponse(medico)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/especialidades")
    public ResponseEntity<List<String>> especialidades() {
        List<String> especialidades = buscarMedicoUseCase.buscarTodos().stream()
                .map(Medico::getEspecialidad)
                .filter(e -> e != null && !e.isBlank())
                .distinct()
                .sorted()
                .toList();
        return ResponseEntity.ok(especialidades);
    }

    @GetMapping("/emails-vinculados")
    public ResponseEntity<List<EmailVinculadoResponse>> emailsVinculados() {
        List<EmailVinculadoResponse> emails = buscarMedicoUseCase.buscarTodos().stream()
                .filter(m -> m.getEmail() != null && !m.getEmail().isBlank())
                .map(m -> new EmailVinculadoResponse(m.getId(), m.getEmail()))
                .toList();
        return ResponseEntity.ok(emails);
    }

    @GetMapping
    public ResponseEntity<PageResponse<MedicoResponse>> buscarTodos(
            @RequestParam(required = false) String especialidad,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Medico> medicos = buscarMedicoUseCase.buscarTodos();
        if (especialidad != null && !especialidad.isBlank()) {
            medicos = medicos.stream().filter(m -> especialidad.equals(m.getEspecialidad())).toList();
        }
        List<MedicoResponse> todos = medicos.stream().map(this::toResponse).toList();
        return ResponseEntity.ok(PageResponse.of(todos, page, size));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicoResponse> actualizar(@PathVariable Long id, @RequestBody MedicoRequest request) {
        CreateMedicoRequest req = new CreateMedicoRequest(
                request.getNombre(),
                request.getEspecialidad(),
                request.getMatricula(),
                request.getDireccion(),
                request.getTelefono(),
                request.getEmail()
        );
        return actualizarMedicoUseCase.actualizar(id, req)
                .map(medico -> ResponseEntity.ok(toResponse(medico)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        boolean eliminado = eliminarMedicoUseCase.eliminar(id);
        return eliminado ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    private MedicoResponse toResponse(Medico medico) {
        return new MedicoResponse(
                medico.getId(),
                medico.getNombre(),
                medico.getEspecialidad(),
                medico.getMatricula(),
                medico.getDireccion(),
                medico.getTelefono(),
                medico.getEmail()
        );
    }
}
