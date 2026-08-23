package com.medconnect.interfaces.rest;

import com.medconnect.application.usecase.ActualizarPacienteUseCase;
import com.medconnect.application.usecase.BuscarPacienteUseCase;
import com.medconnect.application.usecase.CreatePacienteResponse;
import com.medconnect.application.usecase.CrearPacienteUseCase;
import com.medconnect.application.usecase.EliminarPacienteUseCase;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PacienteControllerTest {

    private MockMvc mockMvc;
    private CrearPacienteUseCase crearPacienteUseCase;
    private BuscarPacienteUseCase buscarPacienteUseCase;
    private ActualizarPacienteUseCase actualizarPacienteUseCase;
    private EliminarPacienteUseCase eliminarPacienteUseCase;
    private com.medconnect.application.usecase.BuscarMedicoUseCase buscarMedicoUseCase;
    private com.medconnect.application.usecase.BuscarTurnoUseCase buscarTurnoUseCase;

    @BeforeEach
    public void setup() {
        crearPacienteUseCase = Mockito.mock(CrearPacienteUseCase.class);
        buscarPacienteUseCase = Mockito.mock(BuscarPacienteUseCase.class);
        actualizarPacienteUseCase = Mockito.mock(ActualizarPacienteUseCase.class);
        eliminarPacienteUseCase = Mockito.mock(EliminarPacienteUseCase.class);
        buscarMedicoUseCase = Mockito.mock(com.medconnect.application.usecase.BuscarMedicoUseCase.class);
        buscarTurnoUseCase = Mockito.mock(com.medconnect.application.usecase.BuscarTurnoUseCase.class);
        PacienteController controller = new PacienteController(crearPacienteUseCase, buscarPacienteUseCase, actualizarPacienteUseCase,
                eliminarPacienteUseCase, buscarMedicoUseCase, buscarTurnoUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @org.junit.jupiter.api.AfterEach
    public void tearDown() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
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

    @Test
    public void buscarTodos_filtraPorMedicoLogueado_siRolEsMedico() throws Exception {
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        "medico@medconnect.com", null,
                        List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_MEDICO"))));
        com.medconnect.domain.model.Medico medico = new com.medconnect.domain.model.Medico(2L, null, null, null, null, null, null, null);
        Paciente paciente = new Paciente(3L, "Juan Gómez", "30111222", null, null, null, null, null, null);
        com.medconnect.domain.model.Turno turno = new com.medconnect.domain.model.Turno(
                1L, null, "Cardiología", medico, paciente, null);
        when(buscarMedicoUseCase.buscarPorEmail("medico@medconnect.com")).thenReturn(Optional.of(medico));
        when(buscarTurnoUseCase.buscarPorMedico(2L)).thenReturn(List.of(turno));
        when(buscarPacienteUseCase.buscarPorId(3L)).thenReturn(Optional.of(paciente));

        mockMvc.perform(get("/api/pacientes"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\":3,\"nombre\":\"Juan Gómez\",\"dni\":\"30111222\"}]"));

        Mockito.verify(buscarPacienteUseCase, Mockito.never()).buscarTodos();
    }

    @Test
    public void buscarTodos_devuelveVacio_siRolEsMedicoYNoEstaVinculado() throws Exception {
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        "sin-vincular@medconnect.com", null,
                        List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_MEDICO"))));
        when(buscarMedicoUseCase.buscarPorEmail("sin-vincular@medconnect.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/pacientes"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        Mockito.verify(buscarPacienteUseCase, Mockito.never()).buscarTodos();
    }

    @Test
    public void actualizar_devuelve200_yBody_siExiste() throws Exception {
        Paciente paciente = new Paciente(1L, "Juan Gómez", "30111222", null, null, "OSDE", null, "310", null);
        when(actualizarPacienteUseCase.actualizar(eq(1L), any())).thenReturn(Optional.of(paciente));

        String body = "{\"nombre\":\"Juan Gómez\",\"dni\":\"30111222\",\"obraSocial\":\"OSDE\",\"plan\":\"310\"}";

        mockMvc.perform(put("/api/pacientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":1,\"obraSocial\":\"OSDE\",\"plan\":\"310\"}"));
    }

    @Test
    public void actualizar_devuelve404_siNoExiste() throws Exception {
        when(actualizarPacienteUseCase.actualizar(eq(99L), any())).thenReturn(Optional.empty());

        String body = "{\"nombre\":\"Juan Gómez\",\"dni\":\"30111222\"}";

        mockMvc.perform(put("/api/pacientes/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    public void eliminar_devuelve204_siExiste() throws Exception {
        when(eliminarPacienteUseCase.eliminar(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/pacientes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void eliminar_devuelve404_siNoExiste() throws Exception {
        when(eliminarPacienteUseCase.eliminar(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/pacientes/99"))
                .andExpect(status().isNotFound());
    }
}
