package com.medconnect.interfaces.rest;

import com.medconnect.application.usecase.ActualizarContrasenaUseCase;
import com.medconnect.application.usecase.BuscarUsuarioUseCase;
import com.medconnect.application.usecase.RegistrarUsuarioResponse;
import com.medconnect.application.usecase.RegistrarUsuarioUseCase;
import com.medconnect.domain.exception.UsuarioInvalidoException;
import com.medconnect.domain.model.Usuario;
import com.medconnect.domain.model.UsuarioRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UsuarioControllerTest {

    private MockMvc mockMvc;
    private RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private BuscarUsuarioUseCase buscarUsuarioUseCase;
    private ActualizarContrasenaUseCase actualizarContrasenaUseCase;

    @BeforeEach
    public void setup() {
        registrarUsuarioUseCase = Mockito.mock(RegistrarUsuarioUseCase.class);
        buscarUsuarioUseCase = Mockito.mock(BuscarUsuarioUseCase.class);
        actualizarContrasenaUseCase = Mockito.mock(ActualizarContrasenaUseCase.class);
        UsuarioController controller = new UsuarioController(registrarUsuarioUseCase, buscarUsuarioUseCase, actualizarContrasenaUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void loguearComo(String rol, String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        email, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol))));
    }

    @Test
    public void buscarTodos_devuelveListado_sinContrasena() throws Exception {
        when(buscarUsuarioUseCase.buscarTodos()).thenReturn(List.of(
                new Usuario(1L, "Ana Pérez", "ana@medconnect.com", "hash-secreto", UsuarioRole.MEDICO)));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "[{\"id\":1,\"nombre\":\"Ana Pérez\",\"email\":\"ana@medconnect.com\",\"role\":\"MEDICO\"}]"))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("hash-secreto"))));
    }

    @Test
    public void crear_devuelve201_yBody_siValido_conRoleAdministrador() throws Exception {
        when(registrarUsuarioUseCase.registrarComoAdmin(any())).thenReturn(new RegistrarUsuarioResponse(42L));

        String body = "{\"nombre\":\"Ana Pérez\",\"email\":\"ana@medconnect.com\",\"contrasena\":\"secreto123\",\"role\":\"ADMINISTRADOR\"}";

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(content().json("{\"id\":42}"));
    }

    @Test
    public void crear_devuelve400_siRoleInvalido() throws Exception {
        String body = "{\"nombre\":\"Ana Pérez\",\"email\":\"ana@medconnect.com\",\"contrasena\":\"secreto123\",\"role\":\"NO_EXISTE\"}";

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void crear_devuelve400_siUseCaseRechaza() throws Exception {
        when(registrarUsuarioUseCase.registrarComoAdmin(any()))
                .thenThrow(new UsuarioInvalidoException("ya existe un usuario con ese email"));

        String body = "{\"nombre\":\"Ana Pérez\",\"email\":\"ana@medconnect.com\",\"contrasena\":\"secreto123\",\"role\":\"MEDICO\"}";

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("ya existe un usuario con ese email"));
    }

    @Test
    public void cambiarContrasenaPropia_devuelve204_siValido() throws Exception {
        loguearComo("MEDICO", "medico@medconnect.com");

        String body = "{\"contrasenaActual\":\"vieja123\",\"contrasenaNueva\":\"nueva456\"}";

        mockMvc.perform(patch("/api/usuarios/me/contrasena")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());

        Mockito.verify(actualizarContrasenaUseCase).cambiarPropia(eq("medico@medconnect.com"), any());
    }

    @Test
    public void cambiarContrasenaPropia_devuelve400_siActualIncorrecta() throws Exception {
        loguearComo("MEDICO", "medico@medconnect.com");
        Mockito.doThrow(new UsuarioInvalidoException("la contrasena actual es incorrecta"))
                .when(actualizarContrasenaUseCase).cambiarPropia(eq("medico@medconnect.com"), any());

        String body = "{\"contrasenaActual\":\"mala\",\"contrasenaNueva\":\"nueva456\"}";

        mockMvc.perform(patch("/api/usuarios/me/contrasena")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("la contrasena actual es incorrecta"));
    }

    @Test
    public void resetearContrasena_devuelve204_siExiste() throws Exception {
        when(actualizarContrasenaUseCase.resetearComoAdmin(eq(1L), any())).thenReturn(true);

        String body = "{\"contrasenaNueva\":\"nueva456\"}";

        mockMvc.perform(patch("/api/usuarios/1/contrasena")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());
    }

    @Test
    public void resetearContrasena_devuelve404_siNoExiste() throws Exception {
        when(actualizarContrasenaUseCase.resetearComoAdmin(eq(99L), any())).thenReturn(false);

        String body = "{\"contrasenaNueva\":\"nueva456\"}";

        mockMvc.perform(patch("/api/usuarios/99/contrasena")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }
}
