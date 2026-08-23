package com.medconnect.interfaces.rest;

import com.medconnect.application.usecase.ActualizarEstadoTurnoUseCase;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TurnoControllerTest {

    private MockMvc mockMvc;
    private CrearTurnoUseCase crearTurnoUseCase;
    private BuscarTurnoUseCase buscarTurnoUseCase;
    private ActualizarEstadoTurnoUseCase actualizarEstadoTurnoUseCase;
    private com.medconnect.application.usecase.BuscarMedicoUseCase buscarMedicoUseCase;
    private com.medconnect.application.usecase.BuscarPacienteUseCase buscarPacienteUseCase;

    @BeforeEach
    public void setup() {
        crearTurnoUseCase = Mockito.mock(CrearTurnoUseCase.class);
        buscarTurnoUseCase = Mockito.mock(BuscarTurnoUseCase.class);
        actualizarEstadoTurnoUseCase = Mockito.mock(ActualizarEstadoTurnoUseCase.class);
        buscarMedicoUseCase = Mockito.mock(com.medconnect.application.usecase.BuscarMedicoUseCase.class);
        buscarPacienteUseCase = Mockito.mock(com.medconnect.application.usecase.BuscarPacienteUseCase.class);
        TurnoController controller = new TurnoController(crearTurnoUseCase, buscarTurnoUseCase, actualizarEstadoTurnoUseCase, buscarMedicoUseCase, buscarPacienteUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @org.junit.jupiter.api.AfterEach
    public void tearDown() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    private void loguearComo(String rol, String email) {
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        email, null, List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + rol))));
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
                new Paciente(3L, null, null, null, null, null, null, null, null),
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

    @Test
    public void actualizarEstado_devuelve200_yBody_siExiste() throws Exception {
        Turno turno = new Turno(1L, LocalDateTime.of(2026, 8, 12, 10, 0), "Cardiología",
                new Medico(2L, null, null, null, null, null, null, null),
                new Paciente(3L, null, null, null, null, null, null, null, null),
                TurnoEstado.CONFIRMADO);
        when(actualizarEstadoTurnoUseCase.actualizarEstado(eq(1L), eq(TurnoEstado.CONFIRMADO)))
                .thenReturn(Optional.of(turno));

        mockMvc.perform(patch("/api/turnos/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"CONFIRMADO\"}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":1,\"estado\":\"CONFIRMADO\"}"));
    }

    @Test
    public void actualizarEstado_devuelve404_siNoExiste() throws Exception {
        when(actualizarEstadoTurnoUseCase.actualizarEstado(eq(99L), eq(TurnoEstado.CANCELADO)))
                .thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/turnos/99/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"CANCELADO\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void actualizarEstado_devuelve400_siEstadoInvalido() throws Exception {
        mockMvc.perform(patch("/api/turnos/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"NO_EXISTE\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void actualizarEstado_devuelve400_siUseCaseRechazaTransicion() throws Exception {
        when(actualizarEstadoTurnoUseCase.actualizarEstado(eq(1L), eq(TurnoEstado.CONFIRMADO)))
                .thenThrow(new TurnoInvalidoException("No se puede modificar un turno cancelado"));

        mockMvc.perform(patch("/api/turnos/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"CONFIRMADO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No se puede modificar un turno cancelado"));
    }

    @Test
    public void buscar_ignoraParametros_yFiltraPorMedicoLogueado_siRolEsMedico() throws Exception {
        loguearComo("MEDICO", "medico@medconnect.com");
        when(buscarMedicoUseCase.buscarPorEmail("medico@medconnect.com"))
                .thenReturn(Optional.of(new Medico(2L, null, null, null, null, null, null, null)));
        when(buscarTurnoUseCase.buscarPorMedico(2L)).thenReturn(List.of());

        mockMvc.perform(get("/api/turnos").param("medicoId", "999").param("pacienteId", "888"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        Mockito.verify(buscarTurnoUseCase).buscarPorMedico(2L);
        Mockito.verify(buscarTurnoUseCase, Mockito.never()).buscarPorMedico(999L);
        Mockito.verify(buscarTurnoUseCase, Mockito.never()).buscarTodos();
    }

    @Test
    public void buscar_devuelveVacio_siRolEsMedicoYNoEstaVinculado() throws Exception {
        loguearComo("MEDICO", "sin-vincular@medconnect.com");
        when(buscarMedicoUseCase.buscarPorEmail("sin-vincular@medconnect.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/turnos"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        Mockito.verify(buscarTurnoUseCase, Mockito.never()).buscarTodos();
    }

    @Test
    public void buscar_filtraPorPacienteLogueado_siRolEsPaciente() throws Exception {
        loguearComo("PACIENTE", "paciente@medconnect.com");
        when(buscarPacienteUseCase.buscarPorEmail("paciente@medconnect.com"))
                .thenReturn(Optional.of(new Paciente(3L, null, null, null, null, null, null, null, null)));
        when(buscarTurnoUseCase.buscarPorPaciente(3L)).thenReturn(List.of());

        mockMvc.perform(get("/api/turnos"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        Mockito.verify(buscarTurnoUseCase).buscarPorPaciente(3L);
        Mockito.verify(buscarTurnoUseCase, Mockito.never()).buscarTodos();
    }

    @Test
    public void actualizarEstado_pacientePuedeCancelarSuPropioTurno() throws Exception {
        loguearComo("PACIENTE", "paciente@medconnect.com");
        Paciente paciente = new Paciente(3L, null, null, null, null, null, null, null, null);
        Turno turno = new Turno(1L, LocalDateTime.of(2026, 8, 12, 10, 0), "Cardiología",
                new Medico(2L, null, null, null, null, null, null, null), paciente, TurnoEstado.PENDIENTE);
        when(buscarPacienteUseCase.buscarPorEmail("paciente@medconnect.com")).thenReturn(Optional.of(paciente));
        when(buscarTurnoUseCase.buscarPorId(1L)).thenReturn(Optional.of(turno));
        when(actualizarEstadoTurnoUseCase.actualizarEstado(eq(1L), eq(TurnoEstado.CANCELADO)))
                .thenReturn(Optional.of(turno));

        mockMvc.perform(patch("/api/turnos/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"CANCELADO\"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void actualizarEstado_pacienteNoPuedeConfirmar() throws Exception {
        loguearComo("PACIENTE", "paciente@medconnect.com");

        mockMvc.perform(patch("/api/turnos/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"CONFIRMADO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Un paciente solo puede cancelar su turno"));

        Mockito.verify(actualizarEstadoTurnoUseCase, Mockito.never()).actualizarEstado(any(), any());
    }

    @Test
    public void actualizarEstado_pacienteNoPuedeCancelarTurnoAjeno() throws Exception {
        loguearComo("PACIENTE", "paciente@medconnect.com");
        Paciente pacienteLogueado = new Paciente(3L, null, null, null, null, null, null, null, null);
        Paciente pacienteDelTurno = new Paciente(99L, null, null, null, null, null, null, null, null);
        Turno turno = new Turno(1L, LocalDateTime.of(2026, 8, 12, 10, 0), "Cardiología",
                new Medico(2L, null, null, null, null, null, null, null), pacienteDelTurno, TurnoEstado.PENDIENTE);
        when(buscarPacienteUseCase.buscarPorEmail("paciente@medconnect.com")).thenReturn(Optional.of(pacienteLogueado));
        when(buscarTurnoUseCase.buscarPorId(1L)).thenReturn(Optional.of(turno));

        mockMvc.perform(patch("/api/turnos/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"CANCELADO\"}"))
                .andExpect(status().isForbidden());

        Mockito.verify(actualizarEstadoTurnoUseCase, Mockito.never()).actualizarEstado(any(), any());
    }
}
