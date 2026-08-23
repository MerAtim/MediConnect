package com.medconnect.application.usecase;

import com.medconnect.domain.exception.UsuarioInvalidoException;
import com.medconnect.domain.model.Usuario;
import com.medconnect.domain.model.UsuarioRole;
import com.medconnect.domain.port.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class RegistrarUsuarioServiceTest {

    private static RegistrarUsuarioRequest requestValido() {
        return new RegistrarUsuarioRequest("Ana Pérez", "ana@medconnect.com", "secreto123", UsuarioRole.PACIENTE);
    }

    @Test
    public void registrar_hasheaContrasenaYGuarda() {
        UsuarioRepository repo = Mockito.mock(UsuarioRepository.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);

        when(repo.buscarPorEmail("ana@medconnect.com")).thenReturn(Optional.empty());
        when(encoder.encode("secreto123")).thenReturn("hash-simulado");
        when(repo.guardar(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        RegistrarUsuarioService service = new RegistrarUsuarioService(repo, encoder);

        RegistrarUsuarioResponse resp = service.registrar(requestValido());

        assertEquals(1L, resp.getId());
        Mockito.verify(repo).guardar(Mockito.argThat(u -> "hash-simulado".equals(u.getContrasena())));
    }

    @Test
    public void registrar_lanzaExcepcion_siEmailYaExiste() {
        UsuarioRepository repo = Mockito.mock(UsuarioRepository.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
        when(repo.buscarPorEmail("ana@medconnect.com")).thenReturn(Optional.of(
                new Usuario(1L, "Ana", "ana@medconnect.com", "hash", UsuarioRole.PACIENTE)));

        RegistrarUsuarioService service = new RegistrarUsuarioService(repo, encoder);

        assertThrows(UsuarioInvalidoException.class, () -> service.registrar(requestValido()));
    }

    @Test
    public void registrar_lanzaExcepcion_siEmailInvalido() {
        UsuarioRepository repo = Mockito.mock(UsuarioRepository.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
        RegistrarUsuarioService service = new RegistrarUsuarioService(repo, encoder);

        RegistrarUsuarioRequest req = new RegistrarUsuarioRequest("Ana", "no-es-un-email", "secreto123", UsuarioRole.PACIENTE);

        assertThrows(UsuarioInvalidoException.class, () -> service.registrar(req));
    }

    @Test
    public void registrar_lanzaExcepcion_siContrasenaMuyCorta() {
        UsuarioRepository repo = Mockito.mock(UsuarioRepository.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
        RegistrarUsuarioService service = new RegistrarUsuarioService(repo, encoder);

        RegistrarUsuarioRequest req = new RegistrarUsuarioRequest("Ana", "ana@medconnect.com", "123", UsuarioRole.PACIENTE);

        assertThrows(UsuarioInvalidoException.class, () -> service.registrar(req));
    }

    @Test
    public void registrar_lanzaExcepcion_siFaltaRole() {
        UsuarioRepository repo = Mockito.mock(UsuarioRepository.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
        RegistrarUsuarioService service = new RegistrarUsuarioService(repo, encoder);

        RegistrarUsuarioRequest req = new RegistrarUsuarioRequest("Ana", "ana@medconnect.com", "secreto123", null);

        assertThrows(UsuarioInvalidoException.class, () -> service.registrar(req));
    }

    @Test
    public void registrar_lanzaExcepcion_siRoleEsAdministrador() {
        UsuarioRepository repo = Mockito.mock(UsuarioRepository.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
        RegistrarUsuarioService service = new RegistrarUsuarioService(repo, encoder);

        RegistrarUsuarioRequest req = new RegistrarUsuarioRequest("Ana", "ana@medconnect.com", "secreto123", UsuarioRole.ADMINISTRADOR);

        assertThrows(UsuarioInvalidoException.class, () -> service.registrar(req));
        Mockito.verify(repo, Mockito.never()).guardar(any(Usuario.class));
    }
}
