package com.medconnect.integration;

import com.medconnect.application.usecase.TokenService;
import com.medconnect.domain.model.Medico;
import com.medconnect.domain.model.Paciente;
import com.medconnect.domain.model.Usuario;
import com.medconnect.domain.model.UsuarioRole;
import com.medconnect.domain.port.MedicoRepository;
import com.medconnect.domain.port.PacienteRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// A diferencia del resto de los tests del proyecto (que mockean o fakean los
// repositorios), este arranca un Postgres real en un contenedor Docker: no
// se activa ningun profile especial, asi que Spring conecta los adapters JPA
// reales (@Profile("!test")) contra ese Postgres, corre la migracion V1 de
// Flyway de punta a punta, y deja la cadena de seguridad real intacta
// (ninguna auth deshabilitada). Es el unico lugar del repo donde se prueba
// que el mapeo JPA y la unique constraint de turnos (medico_id, fecha_hora)
// realmente funcionan contra Postgres, no contra un mock o un fake en
// memoria -- antes esta clase excluia el DataSource, usaba fakes y
// deshabilitaba los filtros de seguridad, asi que nunca ejecuto el codigo
// que corre en produccion.
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class CrearTurnoIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private TokenService tokenService;

    private Cookie cookieAdmin() {
        Usuario admin = new Usuario(1L, "Admin Test", "admin.integration@medconnect.com", "hash", UsuarioRole.ADMINISTRADOR);
        return new Cookie("jwt", tokenService.generar(admin));
    }

    @Test
    public void crearTurno_persisteEnPostgresReal_yRespetaLaUniqueConstraint() throws Exception {
        Medico medico = medicoRepository.guardar(new Medico(null, "Ana Pérez", "Cardiología", "MP-IT-1", null, null, null, null));
        Paciente paciente = pacienteRepository.guardar(new Paciente(null, "Juan Gómez", "30111222", null, null, null, null, null, null));

        String body = "{\"fechaHora\":\"2026-08-12T12:00:00\",\"especialidad\":\"Traumatología\",\"medicoId\":"
                + medico.getId() + ",\"pacienteId\":" + paciente.getId() + "}";

        // Sin cookie -> la cadena de seguridad real lo bloquea antes de llegar
        // al controller (no hay DataSource excluido ni addFilters=false aca).
        mockMvc.perform(post("/api/turnos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());

        // POST válido como ADMINISTRADOR -> 201, y el turno realmente queda
        // persistido en Postgres via el adapter JPA real.
        mockMvc.perform(post("/api/turnos").contentType(MediaType.APPLICATION_JSON).content(body)
                        .cookie(cookieAdmin()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());

        mockMvc.perform(get("/api/turnos").param("medicoId", String.valueOf(medico.getId())).cookie(cookieAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].medicoId").value(medico.getId()))
                .andExpect(jsonPath("$.content[0].pacienteId").value(paciente.getId()))
                .andExpect(jsonPath("$.content[0].especialidad").value("Traumatología"))
                .andExpect(jsonPath("$.content[0].estado").value("PENDIENTE"));

        // Mismo medico + misma fecha/hora otra vez -> la unique constraint
        // uk_turnos_medico_fecha (creada por V1__baseline.sql) lo rechaza a
        // nivel de Postgres real, y CrearTurnoService lo traduce al mismo 400
        // de siempre en vez de dejar pasar un 500 crudo de
        // DataIntegrityViolationException.
        mockMvc.perform(post("/api/turnos").contentType(MediaType.APPLICATION_JSON).content(body)
                        .cookie(cookieAdmin()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El médico no está disponible en la fecha y hora solicitada"));
    }
}
