package com.medconnect.interfaces.rest;

import com.medconnect.application.usecase.BuscarMedicoUseCase;
import com.medconnect.application.usecase.CreateMedicoResponse;
import com.medconnect.application.usecase.CrearMedicoUseCase;
import com.medconnect.domain.exception.MedicoInvalidoException;
import com.medconnect.domain.model.Medico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MedicoControllerTest {

    private MockMvc mockMvc;
    private CrearMedicoUseCase crearMedicoUseCase;
    private BuscarMedicoUseCase buscarMedicoUseCase;

    @BeforeEach
    public void setup() {
        crearMedicoUseCase = Mockito.mock(CrearMedicoUseCase.class);
        buscarMedicoUseCase = Mockito.mock(BuscarMedicoUseCase.class);
        MedicoController controller = new MedicoController(crearMedicoUseCase, buscarMedicoUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void crearMedico_devuelve400_siValidacionFalla() throws Exception {
        when(crearMedicoUseCase.crear(any()))
                .thenThrow(new MedicoInvalidoException("nombre es obligatorio"));

        String body = "{\"especialidad\":\"Cardiología\",\"matricula\":\"MP1234\"}";

        mockMvc.perform(post("/api/medicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("nombre es obligatorio"));
    }

    @Test
    public void crearMedico_devuelve201_yBody_siValido() throws Exception {
        when(crearMedicoUseCase.crear(any()))
                .thenReturn(new CreateMedicoResponse(42L));

        String body = "{\"nombre\":\"Ana Pérez\",\"especialidad\":\"Cardiología\",\"matricula\":\"MP1234\"}";

        mockMvc.perform(post("/api/medicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(content().json("{\"id\":42}"));
    }

    @Test
    public void buscarPorId_devuelve200_siExiste() throws Exception {
        Medico medico = new Medico(1L, "Ana Pérez", "Cardiología", "MP1234", null, null, null, null);
        when(buscarMedicoUseCase.buscarPorId(1L)).thenReturn(Optional.of(medico));

        mockMvc.perform(get("/api/medicos/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"id\":1,\"nombre\":\"Ana Pérez\",\"especialidad\":\"Cardiología\",\"matricula\":\"MP1234\"}"));
    }

    @Test
    public void buscarPorId_devuelve404_siNoExiste() throws Exception {
        when(buscarMedicoUseCase.buscarPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/medicos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void buscarTodos_devuelveListado() throws Exception {
        when(buscarMedicoUseCase.buscarTodos()).thenReturn(List.of());

        mockMvc.perform(get("/api/medicos"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
