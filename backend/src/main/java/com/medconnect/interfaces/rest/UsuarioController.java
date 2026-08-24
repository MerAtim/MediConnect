package com.medconnect.interfaces.rest;

import com.medconnect.application.usecase.RegistrarUsuarioRequest;
import com.medconnect.application.usecase.RegistrarUsuarioResponse;
import com.medconnect.application.usecase.RegistrarUsuarioUseCase;
import com.medconnect.domain.exception.UsuarioInvalidoException;
import com.medconnect.domain.model.UsuarioRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;

    public UsuarioController(RegistrarUsuarioUseCase registrarUsuarioUseCase) {
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
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
}
