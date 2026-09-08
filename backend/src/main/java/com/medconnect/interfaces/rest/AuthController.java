package com.medconnect.interfaces.rest;

import com.medconnect.application.usecase.LoginRequest;
import com.medconnect.application.usecase.LoginResponse;
import com.medconnect.application.usecase.LoginUseCase;
import com.medconnect.application.usecase.RegistrarUsuarioRequest;
import com.medconnect.application.usecase.RegistrarUsuarioResponse;
import com.medconnect.application.usecase.RegistrarUsuarioUseCase;
import com.medconnect.application.usecase.TokenRevocationService;
import com.medconnect.domain.exception.UsuarioInvalidoException;
import com.medconnect.domain.model.UsuarioRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    static final String COOKIE_NAME = "jwt";

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final LoginUseCase loginUseCase;
    private final TokenRevocationService tokenRevocationService;
    private final boolean cookieSecure;
    private final long expirationMs;

    public AuthController(RegistrarUsuarioUseCase registrarUsuarioUseCase, LoginUseCase loginUseCase,
                           TokenRevocationService tokenRevocationService,
                           @Value("${app.cookie-secure}") boolean cookieSecure,
                           @Value("${jwt.expiration-ms}") long expirationMs) {
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
        this.loginUseCase = loginUseCase;
        this.tokenRevocationService = tokenRevocationService;
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
        // MEDIUM de la re-auditoria e2e (2026-09-08): antes esto solo
        // borraba la cookie del navegador -- el token en si seguia siendo
        // valido hasta que expiraba solo. Revocar tambien invalida cualquier
        // otra sesion activa con el mismo email (otro navegador/dispositivo),
        // no solo la que esta pidiendo el logout.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            tokenRevocationService.revocarTokensPrevios(auth.getName());
        }
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
