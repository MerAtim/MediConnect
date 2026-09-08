package com.medconnect.application.usecase;

import com.medconnect.domain.exception.UsuarioInvalidoException;
import com.medconnect.domain.model.Usuario;
import com.medconnect.domain.model.UsuarioRole;
import com.medconnect.domain.port.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ActualizarContrasenaServiceTest {

    @Test
    public void cambiarPropia_actualizaHash_siContrasenaActualEsCorrecta() {
        UsuarioRepository repo = Mockito.mock(UsuarioRepository.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
        TokenRevocationService tokenRevocationService = Mockito.mock(TokenRevocationService.class);
        Usuario usuario = new Usuario(1L, "Ana Pérez", "ana@medconnect.com", "hash-viejo", UsuarioRole.MEDICO);
        when(repo.buscarPorEmail("ana@medconnect.com")).thenReturn(Optional.of(usuario));
        when(encoder.matches("vieja123", "hash-viejo")).thenReturn(true);
        when(encoder.encode("nueva456")).thenReturn("hash-nuevo");

        ActualizarContrasenaService service = new ActualizarContrasenaService(repo, encoder, tokenRevocationService);

        service.cambiarPropia("ana@medconnect.com", new CambiarContrasenaRequest("vieja123", "nueva456"));

        assertTrue(usuario.getContrasena().equals("hash-nuevo"));
        verify(repo).guardar(usuario);
    }

    // MEDIUM de la re-auditoria e2e (2026-09-08): "sin revocacion de JWT" --
    // un token robado antes del cambio de contrasena tiene que dejar de
    // servir despues, no solo hasta que expire solo.
    @Test
    public void cambiarPropia_revocaTokensPreviosDelUsuario() {
        UsuarioRepository repo = Mockito.mock(UsuarioRepository.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
        TokenRevocationService tokenRevocationService = Mockito.mock(TokenRevocationService.class);
        Usuario usuario = new Usuario(1L, "Ana Pérez", "ana@medconnect.com", "hash-viejo", UsuarioRole.MEDICO);
        when(repo.buscarPorEmail("ana@medconnect.com")).thenReturn(Optional.of(usuario));
        when(encoder.matches("vieja123", "hash-viejo")).thenReturn(true);
        when(encoder.encode("nueva456")).thenReturn("hash-nuevo");

        ActualizarContrasenaService service = new ActualizarContrasenaService(repo, encoder, tokenRevocationService);

        service.cambiarPropia("ana@medconnect.com", new CambiarContrasenaRequest("vieja123", "nueva456"));

        verify(tokenRevocationService).revocarTokensPrevios("ana@medconnect.com");
    }

    @Test
    public void cambiarPropia_lanzaExcepcion_siActualEsIncorrecta() {
        UsuarioRepository repo = Mockito.mock(UsuarioRepository.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
        TokenRevocationService tokenRevocationService = Mockito.mock(TokenRevocationService.class);
        Usuario usuario = new Usuario(1L, "Ana Pérez", "ana@medconnect.com", "hash-viejo", UsuarioRole.MEDICO);
        when(repo.buscarPorEmail("ana@medconnect.com")).thenReturn(Optional.of(usuario));
        when(encoder.matches("mala", "hash-viejo")).thenReturn(false);

        ActualizarContrasenaService service = new ActualizarContrasenaService(repo, encoder, tokenRevocationService);

        assertThrows(UsuarioInvalidoException.class,
                () -> service.cambiarPropia("ana@medconnect.com", new CambiarContrasenaRequest("mala", "nueva456")));
        verify(repo, never()).guardar(any());
    }

    @Test
    public void cambiarPropia_lanzaExcepcion_siNuevaEsMuyCorta() {
        UsuarioRepository repo = Mockito.mock(UsuarioRepository.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
        TokenRevocationService tokenRevocationService = Mockito.mock(TokenRevocationService.class);
        Usuario usuario = new Usuario(1L, "Ana Pérez", "ana@medconnect.com", "hash-viejo", UsuarioRole.MEDICO);
        when(repo.buscarPorEmail("ana@medconnect.com")).thenReturn(Optional.of(usuario));
        when(encoder.matches("vieja123", "hash-viejo")).thenReturn(true);

        ActualizarContrasenaService service = new ActualizarContrasenaService(repo, encoder, tokenRevocationService);

        assertThrows(UsuarioInvalidoException.class,
                () -> service.cambiarPropia("ana@medconnect.com", new CambiarContrasenaRequest("vieja123", "123")));
        verify(repo, never()).guardar(any());
    }

    @Test
    public void resetearComoAdmin_devuelveTrue_yActualizaHash_siExiste() {
        UsuarioRepository repo = Mockito.mock(UsuarioRepository.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
        TokenRevocationService tokenRevocationService = Mockito.mock(TokenRevocationService.class);
        Usuario usuario = new Usuario(1L, "Ana Pérez", "ana@medconnect.com", "hash-viejo", UsuarioRole.MEDICO);
        when(repo.buscarPorId(1L)).thenReturn(Optional.of(usuario));
        when(encoder.encode("nueva456")).thenReturn("hash-nuevo");

        ActualizarContrasenaService service = new ActualizarContrasenaService(repo, encoder, tokenRevocationService);

        boolean resultado = service.resetearComoAdmin(1L, new ResetearContrasenaRequest("nueva456"));

        assertTrue(resultado);
        assertTrue(usuario.getContrasena().equals("hash-nuevo"));
        verify(repo).guardar(usuario);
    }

    @Test
    public void resetearComoAdmin_revocaTokensPreviosDelUsuario() {
        UsuarioRepository repo = Mockito.mock(UsuarioRepository.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
        TokenRevocationService tokenRevocationService = Mockito.mock(TokenRevocationService.class);
        Usuario usuario = new Usuario(1L, "Ana Pérez", "ana@medconnect.com", "hash-viejo", UsuarioRole.MEDICO);
        when(repo.buscarPorId(1L)).thenReturn(Optional.of(usuario));
        when(encoder.encode("nueva456")).thenReturn("hash-nuevo");

        ActualizarContrasenaService service = new ActualizarContrasenaService(repo, encoder, tokenRevocationService);

        service.resetearComoAdmin(1L, new ResetearContrasenaRequest("nueva456"));

        verify(tokenRevocationService).revocarTokensPrevios("ana@medconnect.com");
    }

    @Test
    public void resetearComoAdmin_devuelveFalse_siNoExiste() {
        UsuarioRepository repo = Mockito.mock(UsuarioRepository.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
        TokenRevocationService tokenRevocationService = Mockito.mock(TokenRevocationService.class);
        when(repo.buscarPorId(99L)).thenReturn(Optional.empty());

        ActualizarContrasenaService service = new ActualizarContrasenaService(repo, encoder, tokenRevocationService);

        boolean resultado = service.resetearComoAdmin(99L, new ResetearContrasenaRequest("nueva456"));

        assertFalse(resultado);
        verify(repo, never()).guardar(any());
    }

    @Test
    public void resetearComoAdmin_lanzaExcepcion_siNuevaEsMuyCorta() {
        UsuarioRepository repo = Mockito.mock(UsuarioRepository.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
        TokenRevocationService tokenRevocationService = Mockito.mock(TokenRevocationService.class);
        Usuario usuario = new Usuario(1L, "Ana Pérez", "ana@medconnect.com", "hash-viejo", UsuarioRole.MEDICO);
        when(repo.buscarPorId(1L)).thenReturn(Optional.of(usuario));

        ActualizarContrasenaService service = new ActualizarContrasenaService(repo, encoder, tokenRevocationService);

        assertThrows(UsuarioInvalidoException.class,
                () -> service.resetearComoAdmin(1L, new ResetearContrasenaRequest("123")));
        verify(repo, never()).guardar(any());
    }
}
