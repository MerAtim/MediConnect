package com.medconnect.integration;

import com.medconnect.domain.model.Medico;
import com.medconnect.domain.model.Paciente;
import com.medconnect.domain.model.Turno;
import com.medconnect.domain.model.Usuario;
import com.medconnect.domain.model.RegistroClinico;
import com.medconnect.domain.port.MedicoRepository;
import com.medconnect.domain.port.PacienteRepository;
import com.medconnect.domain.port.RegistroClinicoRepository;
import com.medconnect.domain.port.TurnoRepository;
import com.medconnect.domain.port.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class CrearTurnoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    // Uses in-memory TurnoRepository defined in TestConfig

    @TestConfiguration
    static class TestConfig {
        @Bean
        public TurnoRepository turnoRepository() {
            return new InMemoryTurnoRepository();
        }

        @Bean
        public com.medconnect.application.usecase.CrearTurnoUseCase crearTurnoUseCase(TurnoRepository repo, MedicoRepository medicoRepo, PacienteRepository pacienteRepo) {
            return new com.medconnect.application.usecase.CrearTurnoService(repo, medicoRepo, pacienteRepo);
        }

        @Bean
        public com.medconnect.application.usecase.BuscarTurnoUseCase buscarTurnoUseCase(TurnoRepository repo) {
            return new com.medconnect.application.usecase.BuscarTurnoService(repo);
        }

        @Bean
        public MedicoRepository medicoRepository() {
            return new InMemoryMedicoRepository();
        }

        @Bean
        public com.medconnect.application.usecase.CrearMedicoUseCase crearMedicoUseCase(MedicoRepository repo) {
            return new com.medconnect.application.usecase.CrearMedicoService(repo);
        }

        @Bean
        public com.medconnect.application.usecase.BuscarMedicoUseCase buscarMedicoUseCase(MedicoRepository repo) {
            return new com.medconnect.application.usecase.BuscarMedicoService(repo);
        }

        @Bean
        public PacienteRepository pacienteRepository() {
            return new InMemoryPacienteRepository();
        }

        @Bean
        public com.medconnect.application.usecase.CrearPacienteUseCase crearPacienteUseCase(PacienteRepository repo) {
            return new com.medconnect.application.usecase.CrearPacienteService(repo);
        }

        @Bean
        public com.medconnect.application.usecase.BuscarPacienteUseCase buscarPacienteUseCase(PacienteRepository repo) {
            return new com.medconnect.application.usecase.BuscarPacienteService(repo);
        }

        @Bean
        public UsuarioRepository usuarioRepository() {
            return new InMemoryUsuarioRepository();
        }

        @Bean
        public RegistroClinicoRepository registroClinicoRepository() {
            return new InMemoryRegistroClinicoRepository();
        }

        @Bean
        public com.medconnect.application.usecase.CrearRegistroClinicoUseCase crearRegistroClinicoUseCase(
                RegistroClinicoRepository repo, MedicoRepository medicoRepo, PacienteRepository pacienteRepo, TurnoRepository turnoRepo) {
            return new com.medconnect.application.usecase.CrearRegistroClinicoService(repo, medicoRepo, pacienteRepo, turnoRepo);
        }

        @Bean
        public com.medconnect.application.usecase.BuscarRegistroClinicoUseCase buscarRegistroClinicoUseCase(RegistroClinicoRepository repo) {
            return new com.medconnect.application.usecase.BuscarRegistroClinicoService(repo);
        }
    }

    @Test
    public void crearTurno_endToEnd() throws Exception {
        Medico medico = medicoRepository.guardar(new Medico(null, "Ana Pérez", "Cardiología", "MP1", null, null, null, null));
        Paciente paciente = pacienteRepository.guardar(new Paciente(null, "Juan Gómez", "30111222", null, null, null, null, null, null));

        // POST válido -> 201
        String body = "{\"fechaHora\":\"2026-08-12T12:00:00\",\"especialidad\":\"Traumatología\",\"medicoId\":" + medico.getId() + ",\"pacienteId\":" + paciente.getId() + "}";

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

    static class InMemoryMedicoRepository implements MedicoRepository {
        private final List<Medico> store = new ArrayList<>();
        private long seq = 1;

        @Override
        public Medico guardar(Medico medico) {
            medico.setId(seq++);
            store.add(medico);
            return medico;
        }

        @Override
        public java.util.Optional<Medico> buscarPorId(Long id) {
            return store.stream().filter(m -> m.getId().equals(id)).findFirst();
        }

        @Override
        public java.util.Optional<Medico> buscarPorEmail(String email) {
            return store.stream().filter(m -> email.equals(m.getEmail())).findFirst();
        }

        @Override
        public List<Medico> buscarTodos() {
            return new ArrayList<>(store);
        }

        @Override
        public void eliminar(Long id) {
            store.removeIf(m -> m.getId().equals(id));
        }
    }

    static class InMemoryPacienteRepository implements PacienteRepository {
        private final List<Paciente> store = new ArrayList<>();
        private long seq = 1;

        @Override
        public Paciente guardar(Paciente paciente) {
            paciente.setId(seq++);
            store.add(paciente);
            return paciente;
        }

        @Override
        public java.util.Optional<Paciente> buscarPorId(Long id) {
            return store.stream().filter(p -> p.getId().equals(id)).findFirst();
        }

        @Override
        public java.util.Optional<Paciente> buscarPorEmail(String email) {
            return store.stream().filter(p -> email.equals(p.getEmail())).findFirst();
        }

        @Override
        public List<Paciente> buscarTodos() {
            return new ArrayList<>(store);
        }

        @Override
        public void eliminar(Long id) {
            store.removeIf(p -> p.getId().equals(id));
        }
    }

    static class InMemoryUsuarioRepository implements UsuarioRepository {
        private final List<Usuario> store = new ArrayList<>();
        private long seq = 1;

        @Override
        public Usuario guardar(Usuario usuario) {
            usuario.setId(seq++);
            store.add(usuario);
            return usuario;
        }

        @Override
        public java.util.Optional<Usuario> buscarPorEmail(String email) {
            return store.stream().filter(u -> u.getEmail().equals(email)).findFirst();
        }
    }

    static class InMemoryRegistroClinicoRepository implements RegistroClinicoRepository {
        private final List<RegistroClinico> store = new ArrayList<>();
        private long seq = 1;

        @Override
        public RegistroClinico guardar(RegistroClinico registro) {
            registro.setId(seq++);
            store.add(registro);
            return registro;
        }

        @Override
        public List<RegistroClinico> buscarPorPaciente(Long pacienteId) {
            List<RegistroClinico> out = new ArrayList<>();
            for (RegistroClinico r : store) {
                if (r.getPaciente() != null && r.getPaciente().getId().equals(pacienteId)) {
                    out.add(r);
                }
            }
            return out;
        }
    }
}
