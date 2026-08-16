package com.medconnect.application.usecase;

import com.medconnect.domain.exception.CredencialesInvalidasException;
import com.medconnect.domain.model.Usuario;
import com.medconnect.domain.model.UsuarioRole;
import com.medconnect.domain.port.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class LoginServiceTest {

    @Test
    public void login_devuelveTokenYDatos_siCredencialesValidas() {
        UsuarioRepository repo = Mockito.mock(UsuarioRepository.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
        TokenService tokenService = Mockito.mock(TokenService.class);

        Usuario usuario = new Usuario(1L, "Ana Pérez", "ana@medconnect.com", "hash", UsuarioRole.PACIENTE);
        when(repo.buscarPorEmail("ana@medconnect.com")).thenReturn(Optional.of(usuario));
        when(encoder.matches("secreto123", "hash")).thenReturn(true);
        when(tokenService.generar(usuario)).thenReturn("token-simulado");

        LoginService service = new LoginService(repo, encoder, tokenService);

        LoginResponse resp = service.login(new LoginRequest("ana@medconnect.com", "secreto123"));

        assertEquals("token-simulado", resp.getToken());
        assertEquals("Ana Pérez", resp.getNombre());
        assertEquals(UsuarioRole.PACIENTE, resp.getRole());
    }

    @Test
    public void login_lanzaExcepcion_siEmailNoExiste() {
        UsuarioRepository repo = Mockito.mock(UsuarioRepository.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
        TokenService tokenService = Mockito.mock(TokenService.class);
        when(repo.buscarPorEmail("no-existe@medconnect.com")).thenReturn(Optional.empty());

        LoginService service = new LoginService(repo, encoder, tokenService);

        assertThrows(CredencialesInvalidasException.class,
                () -> service.login(new LoginRequest("no-existe@medconnect.com", "secreto123")));
    }

    @Test
    public void login_lanzaExcepcion_siContrasenaIncorrecta() {
        UsuarioRepository repo = Mockito.mock(UsuarioRepository.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
        TokenService tokenService = Mockito.mock(TokenService.class);

        Usuario usuario = new Usuario(1L, "Ana Pérez", "ana@medconnect.com", "hash", UsuarioRole.PACIENTE);
        when(repo.buscarPorEmail("ana@medconnect.com")).thenReturn(Optional.of(usuario));
        when(encoder.matches("incorrecta", "hash")).thenReturn(false);

        LoginService service = new LoginService(repo, encoder, tokenService);

        assertThrows(CredencialesInvalidasException.class,
                () -> service.login(new LoginRequest("ana@medconnect.com", "incorrecta")));
    }
}
