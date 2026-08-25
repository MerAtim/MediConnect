package com.medconnect.interfaces.rest;

import com.medconnect.application.usecase.LoginRequest;
import com.medconnect.application.usecase.LoginResponse;
import com.medconnect.application.usecase.LoginUseCase;
import com.medconnect.application.usecase.RegistrarUsuarioRequest;
import com.medconnect.application.usecase.RegistrarUsuarioResponse;
import com.medconnect.application.usecase.RegistrarUsuarioUseCase;
import com.medconnect.domain.exception.UsuarioInvalidoException;
import com.medconnect.domain.model.UsuarioRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    static final String COOKIE_NAME = "jwt";

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final LoginUseCase loginUseCase;
    private final boolean cookieSecure;
    private final long expirationMs;

    public AuthController(RegistrarUsuarioUseCase registrarUsuarioUseCase, LoginUseCase loginUseCase,
                           @Value("${app.cookie-secure}") boolean cookieSecure,
                           @Value("${jwt.expiration-ms}") long expirationMs) {
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
        this.loginUseCase = loginUseCase;
        this.cookieSecure = cookieSecure;
        this.expirationMs = expirationMs;
    }

    @PostMapping("/registro")
    public ResponseEntity<RegistroResponse> registrar(@RequestBody RegistroRequest request) {
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
        RegistrarUsuarioResponse resp = registrarUsuarioUseCase.registrar(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(new RegistroResponse(resp.getId()));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseBody> login(@RequestBody LoginRequestBody request) {
        LoginResponse resp = loginUseCase.login(new LoginRequest(request.getEmail(), request.getContrasena()));
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, resp.getToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMillis(expirationMs))
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new LoginResponseBody(resp.getId(), resp.getNombre(), resp.getEmail(), resp.getRole().name()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
}
