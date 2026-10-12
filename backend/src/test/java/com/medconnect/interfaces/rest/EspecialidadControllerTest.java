package com.medconnect.interfaces.rest;

import com.medconnect.application.usecase.EspecialidadDuracion;
import com.medconnect.application.usecase.GestionarDuracionEspecialidadUseCase;
import com.medconnect.domain.exception.EspecialidadDuracionInvalidaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EspecialidadControllerTest {

    private MockMvc mockMvc;
    private GestionarDuracionEspecialidadUseCase gestionarDuracionEspecialidadUseCase;

    @BeforeEach
    public void setup() {
        gestionarDuracionEspecialidadUseCase = Mockito.mock(GestionarDuracionEspecialidadUseCase.class);
        EspecialidadController controller = new EspecialidadController(gestionarDuracionEspecialidadUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void listarDuraciones_devuelveElListadoDelUseCase() throws Exception {
        when(gestionarDuracionEspecialidadUseCase.listarTodas()).thenReturn(List.of(
                new EspecialidadDuracion("Cardiología", 45),
                new EspecialidadDuracion("Pediatría", 30)));

        mockMvc.perform(get("/api/v1/especialidades/duraciones"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "[{\"especialidad\":\"Cardiología\",\"duracionMinutos\":45},"
                                + "{\"especialidad\":\"Pediatría\",\"duracionMinutos\":30}]"));
    }

    @Test
    public void establecerDuracion_delegaEnElUseCaseConLaEspecialidadDelPathYLosMinutosDelBody() throws Exception {
        String body = "{\"duracionMinutos\":45}";

        mockMvc.perform(put("/api/v1/especialidades/Cardiología/duracion")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());

        verify(gestionarDuracionEspecialidadUseCase).establecer("Cardiología", 45);
    }

    @Test
    public void establecerDuracion_devuelve400_siElUseCaseRechazaLosDatos() throws Exception {
        Mockito.doThrow(new EspecialidadDuracionInvalidaException("duracionMinutos debe ser mayor a cero"))
                .when(gestionarDuracionEspecialidadUseCase).establecer("Cardiología", 0);
        String body = "{\"duracionMinutos\":0}";

        mockMvc.perform(put("/api/v1/especialidades/Cardiología/duracion")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }
}
