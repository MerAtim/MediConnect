package com.medconnect.interfaces.rest;

import com.medconnect.application.usecase.ActualizarContrasenaUseCase;
import com.medconnect.application.usecase.BuscarUsuarioUseCase;
import com.medconnect.application.usecase.CambiarContrasenaRequest;
import com.medconnect.application.usecase.RegistrarUsuarioRequest;
import com.medconnect.application.usecase.RegistrarUsuarioResponse;
import com.medconnect.application.usecase.RegistrarUsuarioUseCase;
import com.medconnect.application.usecase.ResetearContrasenaRequest;
import com.medconnect.domain.exception.UsuarioInvalidoException;
import com.medconnect.domain.model.Usuario;
import com.medconnect.domain.model.UsuarioRole;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final BuscarUsuarioUseCase buscarUsuarioUseCase;
    private final ActualizarContrasenaUseCase actualizarContrasenaUseCase;

    public UsuarioController(RegistrarUsuarioUseCase registrarUsuarioUseCase, BuscarUsuarioUseCase buscarUsuarioUseCase,
                              ActualizarContrasenaUseCase actualizarContrasenaUseCase) {
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
        this.buscarUsuarioUseCase = buscarUsuarioUseCase;
        this.actualizarContrasenaUseCase = actualizarContrasenaUseCase;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> buscarTodos() {
        List<UsuarioResponse> usuarios = buscarUsuarioUseCase.buscarTodos().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(usuarios);
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getRole().name());
    }

    @PostMapping
    public ResponseEntity<RegistroResponse> crear(@RequestBody RegistroRequest request) {
        UsuarioRole role;
        try {
            role = request.getRole() == null ? null : UsuarioRole.valueOf(request.getRole());
        } catch (IllegalArgumentException e) {
            throw new UsuarioInvalidoException("role invalido: " + request.getRole());
        }

        RegistrarUsuarioRequest req = new RegistrarUsuarioRequest(
                request.getNombre(),
                request.getEmail(),
                request.getContrasena(),
                role
        );
        RegistrarUsuarioResponse resp = registrarUsuarioUseCase.registrarComoAdmin(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(new RegistroResponse(resp.getId()));
    }

    @PatchMapping("/me/contrasena")
    public ResponseEntity<Void> cambiarContrasenaPropia(@RequestBody CambiarContrasenaBody body) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CambiarContrasenaRequest req = new CambiarContrasenaRequest(body.getContrasenaActual(), body.getContrasenaNueva());
        actualizarContrasenaUseCase.cambiarPropia(auth.getName(), req);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/contrasena")
    public ResponseEntity<Void> resetearContrasena(@PathVariable Long id, @RequestBody ResetearContrasenaBody body) {
        ResetearContrasenaRequest req = new ResetearContrasenaRequest(body.getContrasenaNueva());
        boolean actualizado = actualizarContrasenaUseCase.resetearComoAdmin(id, req);
        return actualizado ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
