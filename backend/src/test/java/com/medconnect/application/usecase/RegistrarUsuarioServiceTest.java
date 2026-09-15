package com.medconnect.application.usecase;

import com.medconnect.domain.exception.UsuarioInvalidoException;
import com.medconnect.domain.model.Usuario;
import com.medconnect.domain.model.UsuarioRole;
import com.medconnect.domain.port.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// LOW de la re-auditoria e2e (2026-09-08, segunda ronda): los mocks y el
// service se reconstruian desde cero en cada @Test -- boilerplate identico
// por clase. Movidos a campos + @BeforeEach.
public class RegistrarUsuarioServiceTest {

    private UsuarioRepository repo;
    private PasswordEncoder encoder;
    private RegistrarUsuarioService service;

    @BeforeEach
    public void setUp() {
        repo = Mockito.mock(UsuarioRepository.class);
        encoder = Mockito.mock(PasswordEncoder.class);
        service = new RegistrarUsuarioService(repo, encoder);
    }

    private static RegistrarUsuarioRequest requestValido() {
        return new RegistrarUsuarioRequest("Ana Pérez", "ana@medconnect.com", "secreto123", UsuarioRole.PACIENTE);
    }

    @Test
    public void registrar_hasheaContrasenaYGuarda() {
        when(repo.buscarPorEmail("ana@medconnect.com")).thenReturn(Optional.empty());
        when(encoder.encode("secreto123")).thenReturn("hash-simulado");
        when(repo.guardar(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        RegistrarUsuarioResponse resp = service.registrar(requestValido());

        assertEquals(1L, resp.getId());
        Mockito.verify(repo).guardar(Mockito.argThat(u -> "hash-simulado".equals(u.getContrasena())));
    }

    @Test
    public void registrar_lanzaExcepcion_siEmailYaExiste() {
        when(repo.buscarPorEmail("ana@medconnect.com")).thenReturn(Optional.of(
                new Usuario(1L, "Ana", "ana@medconnect.com", "hash", UsuarioRole.PACIENTE)));

        assertThrows(UsuarioInvalidoException.class, () -> service.registrar(requestValido()));
    }

    @Test
    public void registrar_lanzaExcepcion_siEmailInvalido() {
        RegistrarUsuarioRequest req = new RegistrarUsuarioRequest("Ana", "no-es-un-email", "secreto123", UsuarioRole.PACIENTE);

        assertThrows(UsuarioInvalidoException.class, () -> service.registrar(req));
    }

    @Test
    public void registrar_lanzaExcepcion_siContrasenaMuyCorta() {
        RegistrarUsuarioRequest req = new RegistrarUsuarioRequest("Ana", "ana@medconnect.com", "123", UsuarioRole.PACIENTE);

        assertThrows(UsuarioInvalidoException.class, () -> service.registrar(req));
    }

    @Test
    public void registrar_lanzaExcepcion_siFaltaRole() {
        RegistrarUsuarioRequest req = new RegistrarUsuarioRequest("Ana", "ana@medconnect.com", "secreto123", null);

        assertThrows(UsuarioInvalidoException.class, () -> service.registrar(req));
    }

    @Test
    public void registrar_lanzaExcepcion_siRoleEsAdministrador() {
        RegistrarUsuarioRequest req = new RegistrarUsuarioRequest("Ana", "ana@medconnect.com", "secreto123", UsuarioRole.ADMINISTRADOR);

        assertThrows(UsuarioInvalidoException.class, () -> service.registrar(req));
        Mockito.verify(repo, Mockito.never()).guardar(any(Usuario.class));
    }

    @Test
    public void registrar_lanzaExcepcion_siRoleEsMedico() {
        // El autoregistro publico solo puede crear cuentas PACIENTE: una cuenta MEDICO
        // sin vinculacion ni aprobacion de un admin podria leer/escribir historias
        // clinicas de cualquier paciente con solo registrarse.
        RegistrarUsuarioRequest req = new RegistrarUsuarioRequest("Ana", "ana@medconnect.com", "secreto123", UsuarioRole.MEDICO);

        assertThrows(UsuarioInvalidoException.class, () -> service.registrar(req));
        Mockito.verify(repo, Mockito.never()).guardar(any(Usuario.class));
    }

    @Test
    public void registrarComoAdmin_permiteRoleAdministrador() {
        when(repo.buscarPorEmail("ana@medconnect.com")).thenReturn(Optional.empty());
        when(encoder.encode("secreto123")).thenReturn("hash-simulado");
        when(repo.guardar(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        RegistrarUsuarioRequest req = new RegistrarUsuarioRequest("Ana", "ana@medconnect.com", "secreto123", UsuarioRole.ADMINISTRADOR);

        RegistrarUsuarioResponse resp = service.registrarComoAdmin(req);

        assertEquals(1L, resp.getId());
    }
}
