package com.medconnect.interfaces.rest;

import com.medconnect.application.usecase.ActualizarMedicoUseCase;
import com.medconnect.application.usecase.BuscarMedicoUseCase;
import com.medconnect.application.usecase.CreateMedicoResponse;
import com.medconnect.application.usecase.CrearMedicoUseCase;
import com.medconnect.application.usecase.EliminarMedicoUseCase;
import com.medconnect.domain.exception.MedicoInvalidoException;
import com.medconnect.domain.model.Medico;
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

public class MedicoControllerTest {

    private MockMvc mockMvc;
    private CrearMedicoUseCase crearMedicoUseCase;
    private BuscarMedicoUseCase buscarMedicoUseCase;
    private ActualizarMedicoUseCase actualizarMedicoUseCase;
    private EliminarMedicoUseCase eliminarMedicoUseCase;

    @BeforeEach
    public void setup() {
        crearMedicoUseCase = Mockito.mock(CrearMedicoUseCase.class);
        buscarMedicoUseCase = Mockito.mock(BuscarMedicoUseCase.class);
        actualizarMedicoUseCase = Mockito.mock(ActualizarMedicoUseCase.class);
        eliminarMedicoUseCase = Mockito.mock(EliminarMedicoUseCase.class);
        MedicoController controller = new MedicoController(crearMedicoUseCase, buscarMedicoUseCase, actualizarMedicoUseCase, eliminarMedicoUseCase);
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
    public void obtenerPropio_devuelve200_siMedicoEstaVinculado() throws Exception {
        loguearComo("MEDICO", "medico@medconnect.com");
        Medico medico = new Medico(1L, "Ana Pérez", "Cardiología", "MP1234", null, null, "medico@medconnect.com", null);
        when(buscarMedicoUseCase.buscarPorEmail("medico@medconnect.com")).thenReturn(Optional.of(medico));

        mockMvc.perform(get("/api/medicos/me"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":1,\"nombre\":\"Ana Pérez\"}"));
    }

    @Test
    public void obtenerPropio_devuelve404_siMedicoNoEstaVinculado() throws Exception {
        loguearComo("MEDICO", "sin-vincular@medconnect.com");
        when(buscarMedicoUseCase.buscarPorEmail("sin-vincular@medconnect.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/medicos/me"))
                .andExpect(status().isNotFound());
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
    public void buscarTodos_devuelveListadoPaginado() throws Exception {
        when(buscarMedicoUseCase.buscarTodos()).thenReturn(List.of());

        mockMvc.perform(get("/api/medicos"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"content\":[],\"totalElements\":0}"));
    }

    @Test
    public void buscarTodos_filtraPorEspecialidad() throws Exception {
        Medico cardiologa = new Medico(1L, "Ana Pérez", "Cardiología", "MP1234", null, null, null, null);
        Medico clinico = new Medico(2L, "Luis Gómez", "Clínica Médica", "MP5678", null, null, null, null);
        when(buscarMedicoUseCase.buscarTodos()).thenReturn(List.of(cardiologa, clinico));

        mockMvc.perform(get("/api/medicos").param("especialidad", "Cardiología"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"content\":[{\"id\":1,\"nombre\":\"Ana Pérez\"}],\"totalElements\":1}"));
    }

    @Test
    public void especialidades_devuelveListaDistintaYOrdenada() throws Exception {
        Medico m1 = new Medico(1L, "Ana Pérez", "Cardiología", "MP1234", null, null, null, null);
        Medico m2 = new Medico(2L, "Luis Gómez", "Clínica Médica", "MP5678", null, null, null, null);
        Medico m3 = new Medico(3L, "Otra Cardióloga", "Cardiología", "MP9999", null, null, null, null);
        when(buscarMedicoUseCase.buscarTodos()).thenReturn(List.of(m1, m2, m3));

        mockMvc.perform(get("/api/medicos/especialidades"))
                .andExpect(status().isOk())
                .andExpect(content().json("[\"Cardiología\",\"Clínica Médica\"]"));
    }

    @Test
    public void emailsVinculados_omiteMedicosSinEmail() throws Exception {
        Medico conEmail = new Medico(1L, "Ana Pérez", "Cardiología", "MP1234", null, null, "ana@medconnect.com", null);
        Medico sinEmail = new Medico(2L, "Luis Gómez", "Clínica Médica", "MP5678", null, null, null, null);
        when(buscarMedicoUseCase.buscarTodos()).thenReturn(List.of(conEmail, sinEmail));

        mockMvc.perform(get("/api/medicos/emails-vinculados"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\":1,\"email\":\"ana@medconnect.com\"}]"));
    }

    @Test
    public void actualizar_devuelve200_yBody_siExiste() throws Exception {
        Medico medico = new Medico(1L, "Ana Pérez", "Clínica Médica", "MP1234", null, null, null, null);
        when(actualizarMedicoUseCase.actualizar(eq(1L), any())).thenReturn(Optional.of(medico));

        String body = "{\"nombre\":\"Ana Pérez\",\"especialidad\":\"Clínica Médica\",\"matricula\":\"MP1234\"}";

        mockMvc.perform(put("/api/medicos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":1,\"especialidad\":\"Clínica Médica\"}"));
    }

    @Test
    public void actualizar_devuelve404_siNoExiste() throws Exception {
        when(actualizarMedicoUseCase.actualizar(eq(99L), any())).thenReturn(Optional.empty());

        String body = "{\"nombre\":\"Ana Pérez\",\"especialidad\":\"Cardiología\",\"matricula\":\"MP1234\"}";

        mockMvc.perform(put("/api/medicos/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    public void eliminar_devuelve204_siExiste() throws Exception {
        when(eliminarMedicoUseCase.eliminar(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/medicos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void eliminar_devuelve404_siNoExiste() throws Exception {
        when(eliminarMedicoUseCase.eliminar(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/medicos/99"))
                .andExpect(status().isNotFound());
    }
}
