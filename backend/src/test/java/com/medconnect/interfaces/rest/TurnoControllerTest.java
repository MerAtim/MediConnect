package com.medconnect.interfaces.rest;

import com.medconnect.application.usecase.CreateTurnoResponse;
import com.medconnect.application.usecase.CrearTurnoUseCase;
import com.medconnect.domain.exception.TurnoInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TurnoControllerTest {

    private MockMvc mockMvc;
    private CrearTurnoUseCase crearTurnoUseCase;

    @BeforeEach
    public void setup() {
        crearTurnoUseCase = Mockito.mock(CrearTurnoUseCase.class);
        TurnoController controller = new TurnoController(crearTurnoUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void crearTurno_devuelve400_siValidacionFalla() throws Exception {
        when(crearTurnoUseCase.crear(any()))
                .thenThrow(new TurnoInvalidoException("El médico no está disponible"));

        String body = "{\"fechaHora\":\"2026-08-12T10:00:00\",\"especialidad\":\"Cardiología\",\"medicoId\":2,\"pacienteId\":3}";

        mockMvc.perform(post("/api/turnos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El médico no está disponible"));
    }

    @Test
    public void crearTurno_devuelve201_yBody_siValido() throws Exception {
        when(crearTurnoUseCase.crear(any()))
                .thenReturn(new CreateTurnoResponse(42L));

        String body = "{\"fechaHora\":\"2026-08-12T11:00:00\",\"especialidad\":\"Dermatología\",\"medicoId\":5,\"pacienteId\":7}";

        mockMvc.perform(post("/api/turnos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(content().json("{\"id\":42}"));
    }
}
