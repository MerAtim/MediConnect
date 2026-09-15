package com.medconnect.application.usecase;

import com.medconnect.domain.exception.CredencialesInvalidasException;
import com.medconnect.domain.exception.DemasiadosIntentosException;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// LOW de la re-auditoria e2e (2026-09-08, segunda ronda): los mocks se
// reconstruian desde cero en cada @Test -- boilerplate identico por clase.
// Movidos a campos + @BeforeEach. LoginService SI se sigue construyendo
// dentro de cada test (no en @BeforeEach): su constructor llama a
// encoder.encode(...) para precalcular hashDummy, y
// login_llamaAPasswordEncoderMatches_aunQueElEmailNoExista necesita
// configurar ese stub ANTES de construirlo -- moverlo a @BeforeEach
// rompería ese orden y dejaria hashDummy en null.
public class LoginServiceTest {

    private UsuarioRepository repo;
    private PasswordEncoder encoder;
    private TokenService tokenService;
    private LoginRateLimiter rateLimiter;

    @BeforeEach
    public void setUp() {
        repo = Mockito.mock(UsuarioRepository.class);
        encoder = Mockito.mock(PasswordEncoder.class);
        tokenService = Mockito.mock(TokenService.class);
        rateLimiter = Mockito.mock(LoginRateLimiter.class);
    }

    @Test
    public void login_devuelveTokenYDatos_siCredencialesValidas() {
        Usuario usuario = new Usuario(1L, "Ana Pérez", "ana@medconnect.com", "hash", UsuarioRole.PACIENTE);
        when(repo.buscarPorEmail("ana@medconnect.com")).thenReturn(Optional.of(usuario));
        when(encoder.matches("secreto123", "hash")).thenReturn(true);
        when(tokenService.generar(usuario)).thenReturn("token-simulado");

        LoginService service = new LoginService(repo, encoder, tokenService, rateLimiter);

        LoginResponse resp = service.login(new LoginRequest("ana@medconnect.com", "secreto123"));

        assertEquals("token-simulado", resp.getToken());
        assertEquals("Ana Pérez", resp.getNombre());
        assertEquals(UsuarioRole.PACIENTE, resp.getRole());
    }

    @Test
    public void login_lanzaExcepcion_siEmailNoExiste() {
        when(repo.buscarPorEmail("no-existe@medconnect.com")).thenReturn(Optional.empty());

        LoginService service = new LoginService(repo, encoder, tokenService, rateLimiter);

        assertThrows(CredencialesInvalidasException.class,
                () -> service.login(new LoginRequest("no-existe@medconnect.com", "secreto123")));
    }

    @Test
    public void login_lanzaExcepcion_siContrasenaIncorrecta() {
        Usuario usuario = new Usuario(1L, "Ana Pérez", "ana@medconnect.com", "hash", UsuarioRole.PACIENTE);
        when(repo.buscarPorEmail("ana@medconnect.com")).thenReturn(Optional.of(usuario));
        when(encoder.matches("incorrecta", "hash")).thenReturn(false);

        LoginService service = new LoginService(repo, encoder, tokenService, rateLimiter);

        assertThrows(CredencialesInvalidasException.class,
                () -> service.login(new LoginRequest("ana@medconnect.com", "incorrecta")));
    }

    @Test
    public void login_lanzaExcepcion_siRateLimiterBloquea() {
        Mockito.doThrow(new DemasiadosIntentosException("Demasiados intentos fallidos."))
                .when(rateLimiter).verificarPermitido("ana@medconnect.com");

        LoginService service = new LoginService(repo, encoder, tokenService, rateLimiter);

        assertThrows(DemasiadosIntentosException.class,
                () -> service.login(new LoginRequest("ana@medconnect.com", "secreto123")));
        verify(repo, never()).buscarPorEmail(Mockito.anyString());
    }

    @Test
    public void login_registraFalloEnRateLimiter_siCredencialesInvalidas() {
        when(repo.buscarPorEmail("ana@medconnect.com")).thenReturn(Optional.empty());

        LoginService service = new LoginService(repo, encoder, tokenService, rateLimiter);

        assertThrows(CredencialesInvalidasException.class,
                () -> service.login(new LoginRequest("ana@medconnect.com", "secreto123")));
        verify(rateLimiter).registrarFallo("ana@medconnect.com");
        verify(rateLimiter, never()).registrarExito(Mockito.anyString());
    }

    // MEDIUM de la re-auditoria e2e (2026-09-08): antes, si el email no
    // existia, el && de Java cortaba en corto y passwordEncoder.matches
    // (bcrypt, deliberadamente lento) nunca se llamaba -- esa diferencia de
    // tiempo entre "email no existe" (rapido) y "email existe, contrasena
    // incorrecta" (lento) permitia enumerar emails registrados solo midiendo
    // cuanto tarda la respuesta. Ahora matches() se llama siempre, comparando
    // contra un hash dummy si el email no existe.
    @Test
    public void login_llamaAPasswordEncoderMatches_aunQueElEmailNoExista() {
        when(repo.buscarPorEmail("no-existe@medconnect.com")).thenReturn(Optional.empty());
        when(encoder.encode(Mockito.anyString())).thenReturn("hash-dummy");

        LoginService service = new LoginService(repo, encoder, tokenService, rateLimiter);

        assertThrows(CredencialesInvalidasException.class,
                () -> service.login(new LoginRequest("no-existe@medconnect.com", "secreto123")));

        verify(encoder).matches("secreto123", "hash-dummy");
    }

    @Test
    public void login_registraExitoEnRateLimiter_siCredencialesValidas() {
        Usuario usuario = new Usuario(1L, "Ana Pérez", "ana@medconnect.com", "hash", UsuarioRole.PACIENTE);
        when(repo.buscarPorEmail("ana@medconnect.com")).thenReturn(Optional.of(usuario));
        when(encoder.matches("secreto123", "hash")).thenReturn(true);

        LoginService service = new LoginService(repo, encoder, tokenService, rateLimiter);

        service.login(new LoginRequest("ana@medconnect.com", "secreto123"));

        verify(rateLimiter).registrarExito("ana@medconnect.com");
        verify(rateLimiter, never()).registrarFallo(Mockito.anyString());
    }
}
