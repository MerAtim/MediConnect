package com.medconnect.application.usecase;

import com.medconnect.domain.exception.UsuarioInvalidoException;
import com.medconnect.domain.model.Usuario;
import com.medconnect.domain.model.UsuarioRole;
import com.medconnect.domain.port.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegistrarUsuarioService implements RegistrarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrarUsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public RegistrarUsuarioResponse registrar(RegistrarUsuarioRequest request) {
        if (request.getRole() != UsuarioRole.PACIENTE) {
            throw new UsuarioInvalidoException("el autoregistro solo esta disponible para pacientes");
        }
        return guardar(request);
    }

    @Override
    public RegistrarUsuarioResponse registrarComoAdmin(RegistrarUsuarioRequest request) {
        return guardar(request);
    }

    private RegistrarUsuarioResponse guardar(RegistrarUsuarioRequest request) {
        if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
            throw new UsuarioInvalidoException("nombre es obligatorio");
        }
        if (request.getEmail() == null || !request.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new UsuarioInvalidoException("email invalido");
        }
        if (request.getContrasena() == null || request.getContrasena().length() < 6) {
            throw new UsuarioInvalidoException("contrasena debe tener al menos 6 caracteres");
        }
        if (request.getRole() == null) {
            throw new UsuarioInvalidoException("role es obligatorio");
        }
        ValidacionEmail.asegurarDisponible(request.getEmail(), usuarioRepository::buscarPorEmail, Usuario::getId, null,
                () -> new UsuarioInvalidoException("ya existe un usuario con ese email"));

        Usuario usuario = new Usuario(
                null,
                request.getNombre(),
                request.getEmail(),
                passwordEncoder.encode(request.getContrasena()),
                request.getRole()
        );

        Usuario guardado = usuarioRepository.guardar(usuario);
        return new RegistrarUsuarioResponse(guardado.getId());
    }
}
