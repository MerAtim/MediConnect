package com.medconnect.interfaces.rest;

import com.medconnect.application.usecase.LoginResponse;
import com.medconnect.application.usecase.LoginUseCase;
import com.medconnect.application.usecase.RegistrarUsuarioResponse;
import com.medconnect.application.usecase.RegistrarUsuarioUseCase;
import com.medconnect.domain.exception.CredencialesInvalidasException;
import com.medconnect.domain.exception.DemasiadosIntentosException;
import com.medconnect.domain.exception.UsuarioInvalidoException;
import com.medconnect.domain.model.UsuarioRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerTest {

    private MockMvc mockMvc;
    private RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private LoginUseCase loginUseCase;

    @BeforeEach
    public void setup() {
        registrarUsuarioUseCase = Mockito.mock(RegistrarUsuarioUseCase.class);
        loginUseCase = Mockito.mock(LoginUseCase.class);
        AuthController controller = new AuthController(registrarUsuarioUseCase, loginUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void registrar_devuelve201_yBody_siValido() throws Exception {
        when(registrarUsuarioUseCase.registrar(any())).thenReturn(new RegistrarUsuarioResponse(42L));

        String body = "{\"nombre\":\"Ana Pérez\",\"email\":\"ana@medconnect.com\",\"contrasena\":\"secreto123\",\"role\":\"PACIENTE\"}";

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(content().json("{\"id\":42}"));
    }

    @Test
    public void registrar_devuelve400_siRoleInvalido() throws Exception {
        String body = "{\"nombre\":\"Ana Pérez\",\"email\":\"ana@medconnect.com\",\"contrasena\":\"secreto123\",\"role\":\"NO_EXISTE\"}";

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registrar_devuelve400_siUseCaseRechaza() throws Exception {
        when(registrarUsuarioUseCase.registrar(any()))
                .thenThrow(new UsuarioInvalidoException("ya existe un usuario con ese email"));

        String body = "{\"nombre\":\"Ana Pérez\",\"email\":\"ana@medconnect.com\",\"contrasena\":\"secreto123\",\"role\":\"PACIENTE\"}";

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("ya existe un usuario con ese email"));
    }

    @Test
    public void login_devuelve200_yToken_siValido() throws Exception {
        when(loginUseCase.login(any())).thenReturn(
                new LoginResponse("token-simulado", 1L, "Ana Pérez", "ana@medconnect.com", UsuarioRole.PACIENTE));

        String body = "{\"email\":\"ana@medconnect.com\",\"contrasena\":\"secreto123\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"token\":\"token-simulado\",\"id\":1,\"role\":\"PACIENTE\"}"));
    }

    @Test
    public void login_devuelve401_siCredencialesInvalidas() throws Exception {
        when(loginUseCase.login(any()))
                .thenThrow(new CredencialesInvalidasException("email o contraseña incorrectos"));

        String body = "{\"email\":\"ana@medconnect.com\",\"contrasena\":\"mala\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("email o contraseña incorrectos"));
    }

    @Test
    public void login_devuelve429_siDemasiadosIntentos() throws Exception {
        when(loginUseCase.login(any()))
                .thenThrow(new DemasiadosIntentosException("Demasiados intentos fallidos. Probá de nuevo en unos minutos."));

        String body = "{\"email\":\"ana@medconnect.com\",\"contrasena\":\"mala\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().string("Demasiados intentos fallidos. Probá de nuevo en unos minutos."));
    }
}
