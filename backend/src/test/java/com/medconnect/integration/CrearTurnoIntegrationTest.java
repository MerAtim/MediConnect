package com.medconnect.integration;

import com.medconnect.application.usecase.CreateTurnoResponse;
import com.medconnect.domain.model.Turno;
import com.medconnect.domain.port.TurnoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"})
@AutoConfigureMockMvc(addFilters = false)
public class CrearTurnoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // Uses in-memory TurnoRepository defined in TestConfig

    @TestConfiguration
    static class TestConfig {
        @Bean
        public TurnoRepository turnoRepository() {
            return new InMemoryTurnoRepository();
        }

        @Bean
        public com.medconnect.application.usecase.CrearTurnoUseCase crearTurnoUseCase(TurnoRepository repo) {
            return new com.medconnect.application.usecase.CrearTurnoService(repo);
        }
    }

    @Test
    public void crearTurno_endToEnd() throws Exception {
        // POST válido -> 201
        String body = "{\"fechaHora\":\"2026-08-12T12:00:00\",\"especialidad\":\"Traumatología\",\"medicoId\":11,\"pacienteId\":13}";

        mockMvc.perform(post("/api/turnos").contentType("application/json").content(body))
                .andExpect(status().isCreated());
    }

    static class InMemoryTurnoRepository implements TurnoRepository {
        private final List<Turno> store = new ArrayList<>();
        private long seq = 1;

        @Override
        public Turno guardar(Turno turno) {
            turno.setId(seq++);
            store.add(turno);
            return turno;
        }

        @Override
        public java.util.Optional<Turno> buscarPorId(Long id) {
            return store.stream().filter(t -> t.getId().equals(id)).findFirst();
        }

        @Override
        public List<Turno> buscarPorMedico(Long medicoId) {
            List<Turno> out = new ArrayList<>();
            for (Turno t : store) {
                if (t.getMedico() != null && t.getMedico().getId().equals(medicoId)) {
                    out.add(t);
                }
            }
            return out;
        }

        @Override
        public List<Turno> buscarPorPaciente(Long pacienteId) {
            List<Turno> out = new ArrayList<>();
            for (Turno t : store) {
                if (t.getPaciente() != null && t.getPaciente().getId().equals(pacienteId)) {
                    out.add(t);
                }
            }
            return out;
        }

        @Override
        public List<Turno> buscarTodos() {
            return new ArrayList<>(store);
        }
    }
}
