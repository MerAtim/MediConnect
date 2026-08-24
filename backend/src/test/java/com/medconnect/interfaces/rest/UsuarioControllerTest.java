package com.medconnect.interfaces.rest;

import com.medconnect.application.usecase.RegistrarUsuarioResponse;
import com.medconnect.application.usecase.RegistrarUsuarioUseCase;
import com.medconnect.domain.exception.UsuarioInvalidoException;
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

public class UsuarioControllerTest {

    private MockMvc mockMvc;
    private RegistrarUsuarioUseCase registrarUsuarioUseCase;

    @BeforeEach
    public void setup() {
        registrarUsuarioUseCase = Mockito.mock(RegistrarUsuarioUseCase.class);
        UsuarioController controller = new UsuarioController(registrarUsuarioUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
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
}
