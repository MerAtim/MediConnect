package com.medconnect.interfaces.rest;

import com.medconnect.application.usecase.BuscarTurnoUseCase;
import com.medconnect.application.usecase.CreateTurnoResponse;
import com.medconnect.application.usecase.CrearTurnoUseCase;
import com.medconnect.domain.exception.TurnoInvalidoException;
import com.medconnect.domain.model.Medico;
import com.medconnect.domain.model.Paciente;
import com.medconnect.domain.model.Turno;
import com.medconnect.domain.model.TurnoEstado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TurnoControllerTest {

    private MockMvc mockMvc;
    private CrearTurnoUseCase crearTurnoUseCase;
    private BuscarTurnoUseCase buscarTurnoUseCase;

    @BeforeEach
    public void setup() {
        crearTurnoUseCase = Mockito.mock(CrearTurnoUseCase.class);
        buscarTurnoUseCase = Mockito.mock(BuscarTurnoUseCase.class);
        TurnoController controller = new TurnoController(crearTurnoUseCase, buscarTurnoUseCase);
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

    @Test
    public void buscarPorId_devuelve200_siExiste() throws Exception {
        Turno turno = new Turno(1L, LocalDateTime.of(2026, 8, 12, 10, 0), "Cardiología",
                new Medico(2L, null, null, null, null, null, null, null),
                new Paciente(3L, null, null, null, null, null, null),
                TurnoEstado.PENDIENTE);
        when(buscarTurnoUseCase.buscarPorId(1L)).thenReturn(Optional.of(turno));

        mockMvc.perform(get("/api/turnos/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"id\":1,\"especialidad\":\"Cardiología\",\"medicoId\":2,\"pacienteId\":3,\"estado\":\"PENDIENTE\"}"));
    }

    @Test
    public void buscarPorId_devuelve404_siNoExiste() throws Exception {
        when(buscarTurnoUseCase.buscarPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/turnos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void buscar_filtraPorMedicoId_cuandoSePasaComoParametro() throws Exception {
        when(buscarTurnoUseCase.buscarPorMedico(2L)).thenReturn(List.of());

        mockMvc.perform(get("/api/turnos").param("medicoId", "2"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        Mockito.verify(buscarTurnoUseCase).buscarPorMedico(2L);
        Mockito.verify(buscarTurnoUseCase, Mockito.never()).buscarTodos();
    }

    @Test
    public void buscar_devuelveTodos_siNoSePasanParametros() throws Exception {
        when(buscarTurnoUseCase.buscarTodos()).thenReturn(List.of());

        mockMvc.perform(get("/api/turnos"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        Mockito.verify(buscarTurnoUseCase).buscarTodos();
    }
}
