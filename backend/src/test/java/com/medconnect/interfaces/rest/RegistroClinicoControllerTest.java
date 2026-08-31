package com.medconnect.interfaces.rest;

import com.medconnect.application.usecase.BuscarMedicoUseCase;
import com.medconnect.application.usecase.BuscarPacienteUseCase;
import com.medconnect.application.usecase.BuscarRegistroClinicoUseCase;
import com.medconnect.application.usecase.BuscarTurnoUseCase;
import com.medconnect.application.usecase.CreateRegistroClinicoResponse;
import com.medconnect.application.usecase.CrearRegistroClinicoUseCase;
import com.medconnect.domain.exception.RegistroClinicoInvalidoException;
import com.medconnect.domain.model.Medico;
import com.medconnect.domain.model.Paciente;
import com.medconnect.domain.model.RegistroClinico;
import com.medconnect.domain.model.Turno;
import com.medconnect.domain.model.TurnoEstado;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    private BuscarTurnoUseCase buscarTurnoUseCase;

    @BeforeEach
    public void setup() {
        crearRegistroClinicoUseCase = Mockito.mock(CrearRegistroClinicoUseCase.class);
        buscarRegistroClinicoUseCase = Mockito.mock(BuscarRegistroClinicoUseCase.class);
        buscarPacienteUseCase = Mockito.mock(BuscarPacienteUseCase.class);
        buscarMedicoUseCase = Mockito.mock(BuscarMedicoUseCase.class);
        buscarTurnoUseCase = Mockito.mock(BuscarTurnoUseCase.class);
        RegistroClinicoController controller = new RegistroClinicoController(
                crearRegistroClinicoUseCase, buscarRegistroClinicoUseCase, buscarPacienteUseCase,
                buscarMedicoUseCase, buscarTurnoUseCase);
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
    public void crear_devuelve201_yBody_siValido() throws Exception {
        loguearComo("MEDICO", "medico@medconnect.com");
        when(buscarMedicoUseCase.buscarPorEmail("medico@medconnect.com"))
                .thenReturn(Optional.of(new Medico(2L, null, null, null, null, null, null, null)));
        when(crearRegistroClinicoUseCase.crear(any())).thenReturn(new CreateRegistroClinicoResponse(42L));

        String body = "{\"pacienteId\":3,\"diagnostico\":\"Fractura\",\"tratamiento\":\"Reposo\"}";

        mockMvc.perform(post("/api/historias-clinicas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(content().json("{\"id\":42}"));
    }

    @Test
    public void crear_devuelve403_siCuentaNoEstaVinculadaAUnMedico() throws Exception {
        loguearComo("MEDICO", "sin-vincular@medconnect.com");
        when(buscarMedicoUseCase.buscarPorEmail("sin-vincular@medconnect.com")).thenReturn(Optional.empty());

        String body = "{\"pacienteId\":3,\"diagnostico\":\"Fractura\",\"tratamiento\":\"Reposo\"}";

        mockMvc.perform(post("/api/historias-clinicas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        Mockito.verify(crearRegistroClinicoUseCase, Mockito.never()).crear(any());
    }

    @Test
    public void crear_ignoraMedicoIdDelBody_yUsaElDeLaCuentaAutenticada() throws Exception {
        // Un medico no puede hacerse pasar por otro: el medicoId siempre sale del token,
        // nunca del JSON que manda el cliente.
        loguearComo("MEDICO", "medico@medconnect.com");
        when(buscarMedicoUseCase.buscarPorEmail("medico@medconnect.com"))
                .thenReturn(Optional.of(new Medico(2L, null, null, null, null, null, null, null)));
        when(crearRegistroClinicoUseCase.crear(any())).thenReturn(new CreateRegistroClinicoResponse(42L));

        String body = "{\"medicoId\":999,\"pacienteId\":3,\"diagnostico\":\"Fractura\",\"tratamiento\":\"Reposo\"}";

        mockMvc.perform(post("/api/historias-clinicas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        Mockito.verify(crearRegistroClinicoUseCase).crear(
                Mockito.argThat(req -> req.getMedicoId().equals(2L)));
    }

    @Test
    public void crear_devuelve400_siUseCaseRechaza() throws Exception {
        loguearComo("MEDICO", "medico@medconnect.com");
        when(buscarMedicoUseCase.buscarPorEmail("medico@medconnect.com"))
                .thenReturn(Optional.of(new Medico(2L, null, null, null, null, null, null, null)));
        when(crearRegistroClinicoUseCase.crear(any()))
                .thenThrow(new RegistroClinicoInvalidoException("El médico no tiene ningún turno con ese paciente"));

        String body = "{\"pacienteId\":3,\"diagnostico\":\"Fractura\",\"tratamiento\":\"Reposo\"}";

        mockMvc.perform(post("/api/historias-clinicas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El médico no tiene ningún turno con ese paciente"));
    }

    @Test
    public void buscarPorPaciente_devuelveLista_siElMedicoTieneUnTurnoConEsePaciente() throws Exception {
        loguearComo("MEDICO", "medico@medconnect.com");
        Medico medicoLogueado = new Medico(2L, null, null, null, null, null, null, null);
        Paciente paciente = new Paciente(3L, null, null, null, null, null, null, null, null);
        Turno turno = new Turno(10L, LocalDateTime.of(2026, 8, 12, 10, 0), "Cardiología",
                medicoLogueado, paciente, TurnoEstado.CONFIRMADO);
        when(buscarMedicoUseCase.buscarPorEmail("medico@medconnect.com")).thenReturn(Optional.of(medicoLogueado));
        when(buscarTurnoUseCase.buscarPorMedico(2L)).thenReturn(List.of(turno));

        RegistroClinico registro = new RegistroClinico(1L, LocalDateTime.of(2026, 8, 12, 10, 0),
                new Medico(2L, null, null, null, null, null, null, null),
                paciente,
                "Fractura", "Reposo", null);
        when(buscarRegistroClinicoUseCase.buscarPorPaciente(3L)).thenReturn(List.of(registro));

        mockMvc.perform(get("/api/historias-clinicas").param("pacienteId", "3"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "[{\"id\":1,\"medicoId\":2,\"pacienteId\":3,\"diagnostico\":\"Fractura\",\"tratamiento\":\"Reposo\"}]"));
    }

    @Test
    public void buscarPorPaciente_batcheaLaConsultaDeMedicos_enUnaSolaLlamada() throws Exception {
        // Antes, toResponse() llamaba buscarPorId una vez por registro (y
        // formatearDocumento la llamaba dos veces por registro). Este test
        // prueba que el listado ahora resuelve todos los medicos en una sola
        // llamada batch, sin importar cuantos registros haya.
        loguearComo("MEDICO", "medico@medconnect.com");
        Medico medicoLogueado = new Medico(2L, null, null, null, null, null, null, null);
        Paciente paciente = new Paciente(3L, null, null, null, null, null, null, null, null);
        when(buscarMedicoUseCase.buscarPorEmail("medico@medconnect.com")).thenReturn(Optional.of(medicoLogueado));
        when(buscarTurnoUseCase.buscarPorMedico(2L)).thenReturn(
                List.of(new Turno(10L, LocalDateTime.of(2026, 8, 12, 10, 0), "Cardiología", medicoLogueado, paciente, TurnoEstado.CONFIRMADO)));

        Medico medicoA = new Medico(2L, "Dr A", "Cardiología", null, null, null, null, null);
        Medico medicoB = new Medico(7L, "Dr B", "Dermatología", null, null, null, null, null);
        RegistroClinico r1 = new RegistroClinico(1L, LocalDateTime.of(2026, 8, 12, 10, 0), medicoA, paciente, "Fractura", "Reposo", null);
        RegistroClinico r2 = new RegistroClinico(2L, LocalDateTime.of(2026, 8, 13, 10, 0), medicoB, paciente, "Alergia", "Antihistaminico", null);
        when(buscarRegistroClinicoUseCase.buscarPorPaciente(3L)).thenReturn(List.of(r1, r2));
        when(buscarMedicoUseCase.buscarPorIds(List.of(2L, 7L))).thenReturn(Map.of(2L, medicoA, 7L, medicoB));

        mockMvc.perform(get("/api/historias-clinicas").param("pacienteId", "3"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].medicoNombre").value("Dr A"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[1].medicoNombre").value("Dr B"));

        Mockito.verify(buscarMedicoUseCase, Mockito.times(1)).buscarPorIds(any());
        Mockito.verify(buscarMedicoUseCase, Mockito.never()).buscarPorId(any());
    }

    @Test
    public void buscarPorPaciente_devuelve403_siElMedicoNoTieneRelacionConElPaciente() throws Exception {
        // Este es el caso que antes permitia a cualquier MEDICO leer la historia
        // clinica de cualquier paciente sin ninguna relacion (IDOR sobre PHI).
        loguearComo("MEDICO", "medico@medconnect.com");
        Medico medicoLogueado = new Medico(2L, null, null, null, null, null, null, null);
        when(buscarMedicoUseCase.buscarPorEmail("medico@medconnect.com")).thenReturn(Optional.of(medicoLogueado));
        when(buscarTurnoUseCase.buscarPorMedico(2L)).thenReturn(List.of());

        mockMvc.perform(get("/api/historias-clinicas").param("pacienteId", "3"))
                .andExpect(status().isForbidden());

        Mockito.verify(buscarRegistroClinicoUseCase, Mockito.never()).buscarPorPaciente(any());
    }

    @Test
    public void buscarPorPaciente_devuelve403_siCuentaNoEstaVinculadaAUnMedico() throws Exception {
        loguearComo("MEDICO", "sin-vincular@medconnect.com");
        when(buscarMedicoUseCase.buscarPorEmail("sin-vincular@medconnect.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/historias-clinicas").param("pacienteId", "3"))
                .andExpect(status().isForbidden());
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
