package com.medconnect.integration;

import com.medconnect.application.usecase.TokenService;
import com.medconnect.domain.model.Medico;
import com.medconnect.domain.model.Paciente;
import com.medconnect.domain.model.Turno;
import com.medconnect.domain.model.TurnoEstado;
import com.medconnect.domain.model.Usuario;
import com.medconnect.domain.model.UsuarioRole;
import com.medconnect.domain.port.MedicoRepository;
import com.medconnect.domain.port.PacienteRepository;
import com.medconnect.domain.port.TurnoRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// CRITICAL de la re-auditoria e2e (2026-09-08): AesGcmFieldEncryptorTest y
// EncryptedStringConverterTest prueban muy bien el encriptador y el
// converter en aislamiento, pero ningun test corria jamas contra Postgres
// real para confirmar que el converter esta efectivamente APLICADO en la
// entidad -- si alguien pierde una de las tres anotaciones @Convert de
// RegistroClinicoEntity (o agrega un campo nuevo y se olvida de cifrarlo),
// todos los tests unitarios seguian en verde igual, porque prueban el
// converter, no si esta enchufado. Mismo patron que CrearTurnoIntegrationTest
// (Postgres real via Testcontainers, sin fakes, cadena de seguridad real).
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class CrearRegistroClinicoIntegrationTest {

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
    private TurnoRepository turnoRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Cookie cookieMedico(String email) {
        Usuario medico = new Usuario(1L, "Medico Test", email, "hash", UsuarioRole.MEDICO);
        return new Cookie("jwt", tokenService.generar(medico));
    }

    @Test
    public void crearRegistroClinico_quedaCifradoEnLaColumnaCruda_yLegibleViaApi() throws Exception {
        Medico medico = medicoRepository.guardar(
                new Medico(null, "Dr Cifrado", "Clínica", "MP-CIF-1", null, null, "medico.cifrado.it@medconnect.com", null));
        Paciente paciente = pacienteRepository.guardar(
                new Paciente(null, "Pac Cifrado", "30333444", null, null, null, null, null, null));
        // Turno ya ocurrido y no cancelado: habilita el acceso a historia
        // clinica (ver Turno.habilitaHistoriaClinica).
        turnoRepository.guardar(new Turno(null, LocalDateTime.now().minusDays(1), "Clínica",
                medico, paciente, TurnoEstado.CONFIRMADO));

        String diagnosticoOriginal = "Diagnostico sensible de prueba " + System.nanoTime();
        String body = "{\"pacienteId\":" + paciente.getId()
                + ",\"diagnostico\":\"" + diagnosticoOriginal + "\",\"tratamiento\":\"Reposo\"}";

        mockMvc.perform(post("/api/v1/historias-clinicas").contentType(MediaType.APPLICATION_JSON).content(body)
                        .cookie(cookieMedico(medico.getEmail().getValor())))
                .andExpect(status().isCreated());

        // Leer la columna cruda directo por JDBC, sin pasar por el
        // AttributeConverter de JPA -- si el @Convert se pierde en el
        // futuro, esta es la unica asercion del repo que lo detecta.
        String diagnosticoCrudo = jdbcTemplate.queryForObject(
                "SELECT diagnostico FROM registros_clinicos ORDER BY id DESC LIMIT 1", String.class);
        assertNotEquals(diagnosticoOriginal, diagnosticoCrudo);
        assertFalse(diagnosticoCrudo.contains(diagnosticoOriginal));

        // Via la API (que si pasa por el converter) el contenido decifra
        // correctamente al texto original -- confirma que no es solo "esta
        // cifrado", sino que el cifrado es transparente para el resto de la
        // aplicacion.
        mockMvc.perform(get("/api/v1/historias-clinicas").param("pacienteId", String.valueOf(paciente.getId()))
                        .cookie(cookieMedico(medico.getEmail().getValor())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].diagnostico").value(diagnosticoOriginal));
    }
}
