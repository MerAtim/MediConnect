package com.medconnect.interfaces.rest;

import com.medconnect.application.usecase.BuscarMedicoUseCase;
import com.medconnect.application.usecase.BuscarPacienteUseCase;
import com.medconnect.application.usecase.BuscarRegistroClinicoUseCase;
import com.medconnect.application.usecase.CreateRegistroClinicoResponse;
import com.medconnect.application.usecase.CrearRegistroClinicoUseCase;
import com.medconnect.domain.exception.RegistroClinicoInvalidoException;
import com.medconnect.domain.model.Medico;
import com.medconnect.domain.model.Paciente;
import com.medconnect.domain.model.RegistroClinico;
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

public class RegistroClinicoControllerTest {

    private MockMvc mockMvc;
    private CrearRegistroClinicoUseCase crearRegistroClinicoUseCase;
    private BuscarRegistroClinicoUseCase buscarRegistroClinicoUseCase;
    private BuscarPacienteUseCase buscarPacienteUseCase;
    private BuscarMedicoUseCase buscarMedicoUseCase;

    @BeforeEach
    public void setup() {
        crearRegistroClinicoUseCase = Mockito.mock(CrearRegistroClinicoUseCase.class);
        buscarRegistroClinicoUseCase = Mockito.mock(BuscarRegistroClinicoUseCase.class);
        buscarPacienteUseCase = Mockito.mock(BuscarPacienteUseCase.class);
        buscarMedicoUseCase = Mockito.mock(BuscarMedicoUseCase.class);
        RegistroClinicoController controller = new RegistroClinicoController(
                crearRegistroClinicoUseCase, buscarRegistroClinicoUseCase, buscarPacienteUseCase, buscarMedicoUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void crear_devuelve201_yBody_siValido() throws Exception {
        when(crearRegistroClinicoUseCase.crear(any())).thenReturn(new CreateRegistroClinicoResponse(42L));

        String body = "{\"medicoId\":2,\"pacienteId\":3,\"diagnostico\":\"Fractura\",\"tratamiento\":\"Reposo\"}";

        mockMvc.perform(post("/api/historias-clinicas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(content().json("{\"id\":42}"));
    }

    @Test
    public void crear_devuelve400_siUseCaseRechaza() throws Exception {
        when(crearRegistroClinicoUseCase.crear(any()))
                .thenThrow(new RegistroClinicoInvalidoException("El médico no tiene ningún turno con ese paciente"));

        String body = "{\"medicoId\":2,\"pacienteId\":3,\"diagnostico\":\"Fractura\",\"tratamiento\":\"Reposo\"}";

        mockMvc.perform(post("/api/historias-clinicas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El médico no tiene ningún turno con ese paciente"));
    }

    @Test
    public void buscarPorPaciente_devuelveLista() throws Exception {
        RegistroClinico registro = new RegistroClinico(1L, LocalDateTime.of(2026, 8, 12, 10, 0),
                new Medico(2L, null, null, null, null, null, null, null),
                new Paciente(3L, null, null, null, null, null, null, null, null),
                "Fractura", "Reposo", null);
        when(buscarRegistroClinicoUseCase.buscarPorPaciente(3L)).thenReturn(List.of(registro));

        mockMvc.perform(get("/api/historias-clinicas").param("pacienteId", "3"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "[{\"id\":1,\"medicoId\":2,\"pacienteId\":3,\"diagnostico\":\"Fractura\",\"tratamiento\":\"Reposo\"}]"));
    }

    @Test
    public void exportar_devuelve404_siPacienteNoExiste() throws Exception {
        when(buscarPacienteUseCase.buscarPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/historias-clinicas/exportar").param("pacienteId", "99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void exportar_devuelve200_yArchivoDescargable_siPacienteExiste() throws Exception {
        when(buscarPacienteUseCase.buscarPorId(3L)).thenReturn(
                Optional.of(new Paciente(3L, "Juan Gómez", "30111222", null, null, null, null, null, null)));
        when(buscarRegistroClinicoUseCase.buscarPorPaciente(3L)).thenReturn(List.of());

        mockMvc.perform(get("/api/historias-clinicas/exportar").param("pacienteId", "3"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")));
    }
}
