package com.medconnect.interfaces.rest;

import com.medconnect.application.usecase.BuscarPacienteUseCase;
import com.medconnect.application.usecase.CreatePacienteResponse;
import com.medconnect.application.usecase.CrearPacienteUseCase;
import com.medconnect.domain.exception.PacienteInvalidoException;
import com.medconnect.domain.model.Paciente;
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

public class PacienteControllerTest {

    private MockMvc mockMvc;
    private CrearPacienteUseCase crearPacienteUseCase;
    private BuscarPacienteUseCase buscarPacienteUseCase;

    @BeforeEach
    public void setup() {
        crearPacienteUseCase = Mockito.mock(CrearPacienteUseCase.class);
        buscarPacienteUseCase = Mockito.mock(BuscarPacienteUseCase.class);
        PacienteController controller = new PacienteController(crearPacienteUseCase, buscarPacienteUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void crearPaciente_devuelve400_siValidacionFalla() throws Exception {
        when(crearPacienteUseCase.crear(any()))
                .thenThrow(new PacienteInvalidoException("dni es obligatorio"));

        String body = "{\"nombre\":\"Juan Gómez\"}";

        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("dni es obligatorio"));
    }

    @Test
    public void crearPaciente_devuelve201_yBody_siValido() throws Exception {
        when(crearPacienteUseCase.crear(any()))
                .thenReturn(new CreatePacienteResponse(42L));

        String body = "{\"nombre\":\"Juan Gómez\",\"dni\":\"30111222\"}";

        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(content().json("{\"id\":42}"));
    }

    @Test
    public void buscarPorId_devuelve200_siExiste() throws Exception {
        Paciente paciente = new Paciente(1L, "Juan Gómez", "30111222", null, null, "Swiss Medical", "123456", "SMG20", null);
        when(buscarPacienteUseCase.buscarPorId(1L)).thenReturn(Optional.of(paciente));

        mockMvc.perform(get("/api/pacientes/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"id\":1,\"nombre\":\"Juan Gómez\",\"dni\":\"30111222\",\"obraSocial\":\"Swiss Medical\",\"numeroAfiliado\":\"123456\",\"plan\":\"SMG20\"}"));
    }

    @Test
    public void buscarPorId_devuelve404_siNoExiste() throws Exception {
        when(buscarPacienteUseCase.buscarPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/pacientes/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void buscarTodos_devuelveListado() throws Exception {
        when(buscarPacienteUseCase.buscarTodos()).thenReturn(List.of());

        mockMvc.perform(get("/api/pacientes"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
