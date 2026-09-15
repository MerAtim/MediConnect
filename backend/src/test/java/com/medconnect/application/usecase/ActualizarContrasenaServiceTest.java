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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// LOW de la re-auditoria e2e (2026-09-08, segunda ronda): los mocks
// (repo/encoder/tokenRevocationService) y el service se reconstruian desde
// cero en cada @Test -- 15-30 lineas de boilerplate identico por clase que
// ocultaba cual mock era realmente relevante en cada caso puntual. Movidos
// a campos + @BeforeEach, dejando en el cuerpo de cada test solo el
// when(...) especifico del caso.
public class ActualizarContrasenaServiceTest {

    private UsuarioRepository repo;
    private PasswordEncoder encoder;
    private TokenRevocationService tokenRevocationService;
    private ActualizarContrasenaService service;

    @BeforeEach
    public void setUp() {
        repo = Mockito.mock(UsuarioRepository.class);
        encoder = Mockito.mock(PasswordEncoder.class);
        tokenRevocationService = Mockito.mock(TokenRevocationService.class);
        service = new ActualizarContrasenaService(repo, encoder, tokenRevocationService);
    }

    @Test
    public void cambiarPropia_actualizaHash_siContrasenaActualEsCorrecta() {
        Usuario usuario = new Usuario(1L, "Ana Pérez", "ana@medconnect.com", "hash-viejo", UsuarioRole.MEDICO);
        when(repo.buscarPorEmail("ana@medconnect.com")).thenReturn(Optional.of(usuario));
        when(encoder.matches("vieja123", "hash-viejo")).thenReturn(true);
        when(encoder.encode("nueva456")).thenReturn("hash-nuevo");

        service.cambiarPropia("ana@medconnect.com", new CambiarContrasenaRequest("vieja123", "nueva456"));

        assertEquals("hash-nuevo", usuario.getContrasena());
        verify(repo).guardar(usuario);
    }

    // MEDIUM de la re-auditoria e2e (2026-09-08): "sin revocacion de JWT" --
    // un token robado antes del cambio de contrasena tiene que dejar de
    // servir despues, no solo hasta que expire solo.
    @Test
    public void cambiarPropia_revocaTokensPreviosDelUsuario() {
        Usuario usuario = new Usuario(1L, "Ana Pérez", "ana@medconnect.com", "hash-viejo", UsuarioRole.MEDICO);
        when(repo.buscarPorEmail("ana@medconnect.com")).thenReturn(Optional.of(usuario));
        when(encoder.matches("vieja123", "hash-viejo")).thenReturn(true);
        when(encoder.encode("nueva456")).thenReturn("hash-nuevo");

        service.cambiarPropia("ana@medconnect.com", new CambiarContrasenaRequest("vieja123", "nueva456"));

        verify(tokenRevocationService).revocarTokensPrevios("ana@medconnect.com");
    }

    @Test
    public void cambiarPropia_lanzaExcepcion_siActualEsIncorrecta() {
        Usuario usuario = new Usuario(1L, "Ana Pérez", "ana@medconnect.com", "hash-viejo", UsuarioRole.MEDICO);
        when(repo.buscarPorEmail("ana@medconnect.com")).thenReturn(Optional.of(usuario));
        when(encoder.matches("mala", "hash-viejo")).thenReturn(false);

        assertThrows(UsuarioInvalidoException.class,
                () -> service.cambiarPropia("ana@medconnect.com", new CambiarContrasenaRequest("mala", "nueva456")));
        verify(repo, never()).guardar(any());
    }

    @Test
    public void cambiarPropia_lanzaExcepcion_siNuevaEsMuyCorta() {
        Usuario usuario = new Usuario(1L, "Ana Pérez", "ana@medconnect.com", "hash-viejo", UsuarioRole.MEDICO);
        when(repo.buscarPorEmail("ana@medconnect.com")).thenReturn(Optional.of(usuario));
        when(encoder.matches("vieja123", "hash-viejo")).thenReturn(true);

        assertThrows(UsuarioInvalidoException.class,
                () -> service.cambiarPropia("ana@medconnect.com", new CambiarContrasenaRequest("vieja123", "123")));
        verify(repo, never()).guardar(any());
    }

    @Test
    public void resetearComoAdmin_devuelveTrue_yActualizaHash_siExiste() {
        Usuario usuario = new Usuario(1L, "Ana Pérez", "ana@medconnect.com", "hash-viejo", UsuarioRole.MEDICO);
        when(repo.buscarPorId(1L)).thenReturn(Optional.of(usuario));
        when(encoder.encode("nueva456")).thenReturn("hash-nuevo");

        boolean resultado = service.resetearComoAdmin(1L, new ResetearContrasenaRequest("nueva456"));

        assertTrue(resultado);
        assertEquals("hash-nuevo", usuario.getContrasena());
        verify(repo).guardar(usuario);
    }

    @Test
    public void resetearComoAdmin_revocaTokensPreviosDelUsuario() {
        Usuario usuario = new Usuario(1L, "Ana Pérez", "ana@medconnect.com", "hash-viejo", UsuarioRole.MEDICO);
        when(repo.buscarPorId(1L)).thenReturn(Optional.of(usuario));
        when(encoder.encode("nueva456")).thenReturn("hash-nuevo");

        service.resetearComoAdmin(1L, new ResetearContrasenaRequest("nueva456"));

        verify(tokenRevocationService).revocarTokensPrevios("ana@medconnect.com");
    }

    @Test
    public void resetearComoAdmin_devuelveFalse_siNoExiste() {
        when(repo.buscarPorId(99L)).thenReturn(Optional.empty());

        boolean resultado = service.resetearComoAdmin(99L, new ResetearContrasenaRequest("nueva456"));

        assertFalse(resultado);
        verify(repo, never()).guardar(any());
    }

    @Test
    public void resetearComoAdmin_lanzaExcepcion_siNuevaEsMuyCorta() {
        Usuario usuario = new Usuario(1L, "Ana Pérez", "ana@medconnect.com", "hash-viejo", UsuarioRole.MEDICO);
        when(repo.buscarPorId(1L)).thenReturn(Optional.of(usuario));

        assertThrows(UsuarioInvalidoException.class,
                () -> service.resetearComoAdmin(1L, new ResetearContrasenaRequest("123")));
        verify(repo, never()).guardar(any());
    }
}
