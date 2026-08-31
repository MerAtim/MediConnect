package com.medconnect.application.usecase;

import com.medconnect.domain.exception.CredencialesInvalidasException;
import com.medconnect.domain.model.Usuario;
import com.medconnect.domain.port.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginService implements LoginUseCase {

    private static final Logger log = LoggerFactory.getLogger(LoginService.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final LoginRateLimiter loginRateLimiter;

    public LoginService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, TokenService tokenService,
                         LoginRateLimiter loginRateLimiter) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.loginRateLimiter = loginRateLimiter;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail() == null ? "" : request.getEmail();
        loginRateLimiter.verificarPermitido(email);

        Optional<Usuario> usuarioOpt = usuarioRepository.buscarPorEmail(email);
        boolean credencialesValidas = usuarioOpt.isPresent() && request.getContrasena() != null
                && passwordEncoder.matches(request.getContrasena(), usuarioOpt.get().getContrasena());

        if (!credencialesValidas) {
            loginRateLimiter.registrarFallo(email);
            log.warn("Login fallido: email={}", email);
            throw new CredencialesInvalidasException("email o contraseña incorrectos");
        }

        loginRateLimiter.registrarExito(email);
        Usuario usuario = usuarioOpt.get();
        log.info("Login exitoso: usuarioId={} email={} role={}", usuario.getId(), usuario.getEmail(), usuario.getRole());
        String token = tokenService.generar(usuario);
        return new LoginResponse(token, usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getRole());
    }
}
