package com.medconnect.application.usecase;

import com.medconnect.domain.exception.UsuarioInvalidoException;
import com.medconnect.domain.model.Usuario;
import com.medconnect.domain.port.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ActualizarContrasenaService implements ActualizarContrasenaUseCase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRevocationService tokenRevocationService;

    public ActualizarContrasenaService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                                        TokenRevocationService tokenRevocationService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenRevocationService = tokenRevocationService;
    }

    @Override
    public void cambiarPropia(String email, CambiarContrasenaRequest request) {
        Usuario usuario = usuarioRepository.buscarPorEmail(email)
                .orElseThrow(() -> new UsuarioInvalidoException("usuario no encontrado"));

        if (request.getContrasenaActual() == null
                || !passwordEncoder.matches(request.getContrasenaActual(), usuario.getContrasena())) {
            throw new UsuarioInvalidoException("la contrasena actual es incorrecta");
        }

        validarNueva(request.getContrasenaNueva());
        usuario.setContrasena(passwordEncoder.encode(request.getContrasenaNueva()));
        usuarioRepository.guardar(usuario);
        // MEDIUM de la re-auditoria e2e (2026-09-08): sin esto, un token
        // robado antes del cambio de contrasena seguia siendo valido despues
        // -- cambiar la contrasena no alcanzaba para desactivar una sesion
        // ya comprometida.
        tokenRevocationService.revocarTokensPrevios(usuario.getEmail().getValor());
    }

    @Override
    public boolean resetearComoAdmin(Long usuarioId, ResetearContrasenaRequest request) {
        Optional<Usuario> usuarioOpt = usuarioRepository.buscarPorId(usuarioId);
        if (usuarioOpt.isEmpty()) {
            return false;
        }

        validarNueva(request.getContrasenaNueva());
        Usuario usuario = usuarioOpt.get();
        usuario.setContrasena(passwordEncoder.encode(request.getContrasenaNueva()));
        usuarioRepository.guardar(usuario);
        tokenRevocationService.revocarTokensPrevios(usuario.getEmail().getValor());
        return true;
    }

    private void validarNueva(String contrasenaNueva) {
        if (contrasenaNueva == null || contrasenaNueva.length() < 6) {
            throw new UsuarioInvalidoException("contrasena debe tener al menos 6 caracteres");
        }
    }
}
