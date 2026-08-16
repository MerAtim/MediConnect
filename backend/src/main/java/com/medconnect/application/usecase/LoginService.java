package com.medconnect.application.usecase;

import com.medconnect.domain.exception.CredencialesInvalidasException;
import com.medconnect.domain.model.Usuario;
import com.medconnect.domain.port.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService implements LoginUseCase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public LoginService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.buscarPorEmail(request.getEmail() == null ? "" : request.getEmail())
                .orElseThrow(() -> new CredencialesInvalidasException("email o contraseña incorrectos"));

        if (request.getContrasena() == null || !passwordEncoder.matches(request.getContrasena(), usuario.getContrasena())) {
            throw new CredencialesInvalidasException("email o contraseña incorrectos");
        }

        String token = tokenService.generar(usuario);
        return new LoginResponse(token, usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getRole());
    }
}
