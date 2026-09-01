package com.medconnect.infrastructure.config;

import com.medconnect.application.usecase.TokenService;
import com.medconnect.domain.model.Medico;
import com.medconnect.domain.model.Paciente;
import com.medconnect.domain.model.RegistroClinico;
import com.medconnect.domain.model.Turno;
import com.medconnect.domain.model.TurnoEstado;
import com.medconnect.domain.model.Usuario;
import com.medconnect.domain.model.UsuarioRole;
import com.medconnect.domain.port.MedicoRepository;
import com.medconnect.domain.port.PacienteRepository;
import com.medconnect.domain.port.RegistroClinicoRepository;
import com.medconnect.domain.port.TurnoRepository;
import com.medconnect.domain.port.UsuarioRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Los *ControllerTest usan standaloneSetup: nunca cargan SecurityConfig ni
// JwtAuthenticationFilter, asi que las reglas hasRole/hasAnyRole de
// SecurityConfig no tenian NINGUN test que las ejercitara de punta a punta.
// Este test si carga la cadena real de filtros (@AutoConfigureMockMvc sin
// addFilters=false) y autentica con cookies JWT reales generadas por el
// TokenService real -- si alguien afloja o endurece una regla de rol por
// error, esto se rompe.
@SpringBootTest(properties = {"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private TurnoRepository turnoRepository;

    @TestConfiguration
    static class TestConfig {
        @Bean
        MedicoRepository medicoRepository() {
            return new InMemoryMedicoRepository();
        }

        @Bean
        PacienteRepository pacienteRepository() {
            return new InMemoryPacienteRepository();
        }

        @Bean
        TurnoRepository turnoRepository() {
            return new InMemoryTurnoRepository();
        }

        @Bean
        UsuarioRepository usuarioRepository() {
            return new InMemoryUsuarioRepository();
        }

        @Bean
        RegistroClinicoRepository registroClinicoRepository() {
            return new InMemoryRegistroClinicoRepository();
        }
    }

    private Cookie jwtCookie(UsuarioRole role) {
        return jwtCookie(role, "generico." + role.name().toLowerCase() + "@medconnect.com");
    }

    // Para los casos donde el endpoint ademas exige pertenencia (el medico
    // tiene que tener un turno con ese paciente, etc.) y hace falta que el
    // email del token coincida con un Medico/Paciente sembrado en el repo.
    private Cookie jwtCookie(UsuarioRole role, String email) {
        Usuario usuario = new Usuario(1L, "Test", email, "hash", role);
        return new Cookie("jwt", tokenService.generar(usuario));
    }

    // No es 401 (no autenticado) ni 403 (rol sin permiso): la autorizacion
    // dejo pasar el request al controller, sea cual sea el status final.
    private static ResultMatcher noRechazadoPorAutorizacion() {
        return result -> {
            int status = result.getResponse().getStatus();
            if (status == 401 || status == 403) {
                throw new AssertionError("Se esperaba pasar la autorizacion, pero se recibio " + status);
            }
        };
    }

    @Test
    public void actuatorHealth_esPublico_peroElRestoDeActuatorNo() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/actuator/env"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/actuator/env").cookie(jwtCookie(UsuarioRole.ADMINISTRADOR)))
                .andExpect(noRechazadoPorAutorizacion());
    }

    @Test
    public void login_esPublico() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nadie@medconnect.com\",\"contrasena\":\"x\"}"))
                // 401 por credenciales invalidas (CredencialesInvalidasException), nunca por
                // falta de autenticacion previa -- /api/auth/** es permitAll.
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getMedicos_requiereRolAdministrador() throws Exception {
        mockMvc.perform(get("/api/medicos"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/medicos").cookie(jwtCookie(UsuarioRole.MEDICO)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/medicos").cookie(jwtCookie(UsuarioRole.PACIENTE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/medicos").cookie(jwtCookie(UsuarioRole.ADMINISTRADOR)))
                .andExpect(status().isOk());
    }

    @Test
    public void getPacientes_permiteAdministradorYMedico_noPaciente() throws Exception {
        mockMvc.perform(get("/api/pacientes"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/pacientes").cookie(jwtCookie(UsuarioRole.PACIENTE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/pacientes").cookie(jwtCookie(UsuarioRole.ADMINISTRADOR)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/pacientes").cookie(jwtCookie(UsuarioRole.MEDICO)))
                .andExpect(status().isOk());
    }

    @Test
    public void postUsuarios_requiereRolAdministrador() throws Exception {
        String body = "{\"nombre\":\"X\",\"email\":\"x@medconnect.com\",\"contrasena\":\"secreto123\",\"role\":\"PACIENTE\"}";
        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(body)
                        .cookie(jwtCookie(UsuarioRole.MEDICO)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(body)
                        .cookie(jwtCookie(UsuarioRole.ADMINISTRADOR)))
                .andExpect(status().isCreated());
    }

    @Test
    public void postTurnos_requiereRolAdministrador() throws Exception {
        String body = "{\"fechaHora\":\"2026-09-01T10:00:00\",\"especialidad\":\"Clinica\",\"medicoId\":1,\"pacienteId\":1}";
        mockMvc.perform(post("/api/turnos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/turnos").contentType(MediaType.APPLICATION_JSON).content(body)
                        .cookie(jwtCookie(UsuarioRole.MEDICO)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/turnos").contentType(MediaType.APPLICATION_JSON).content(body)
                        .cookie(jwtCookie(UsuarioRole.ADMINISTRADOR)))
                .andExpect(noRechazadoPorAutorizacion());
    }

    @Test
    public void patchEstadoTurno_permiteCualquierRolAutenticado() throws Exception {
        // Ademas del rol, el endpoint exige pertenencia para MEDICO/PACIENTE:
        // sembramos un turno real para que "no rechazado por autorizacion"
        // pruebe el camino completo, no solo la regla de SecurityConfig.
        Medico medico = medicoRepository.guardar(new Medico(null, "Dr Turno", "Clinica", "MTUR-1", null, null, "medico.turno.sec@medconnect.com", null));
        Paciente paciente = pacienteRepository.guardar(new Paciente(null, "Pac Turno", "1", null, null, null, null, null, "paciente.turno.sec@medconnect.com"));
        Turno turno = turnoRepository.guardar(new Turno(null, LocalDateTime.now(), "Clinica", medico, paciente, TurnoEstado.PENDIENTE));
        String url = "/api/turnos/" + turno.getId() + "/estado";

        mockMvc.perform(patch(url).contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"CONFIRMADO\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch(url).contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"CONFIRMADO\"}")
                        .cookie(jwtCookie(UsuarioRole.ADMINISTRADOR)))
                .andExpect(noRechazadoPorAutorizacion());
        mockMvc.perform(patch(url).contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"CONFIRMADO\"}")
                        .cookie(jwtCookie(UsuarioRole.MEDICO, medico.getEmail())))
                .andExpect(noRechazadoPorAutorizacion());
        mockMvc.perform(patch(url).contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"CANCELADO\"}")
                        .cookie(jwtCookie(UsuarioRole.PACIENTE, paciente.getEmail())))
                .andExpect(noRechazadoPorAutorizacion());
    }

    @Test
    public void getHistoriasClinicas_requiereRolMedico() throws Exception {
        // esPacienteDeEseMedico exige un turno real entre ambos para dejar
        // pasar a un MEDICO -- lo sembramos para que el caso permitido de
        // este test pruebe el camino completo (rol + pertenencia), no solo
        // que la regla de rol no lo bloquee.
        Medico medico = medicoRepository.guardar(new Medico(null, "Dr Historia", "Clinica", "MHIST-1", null, null, "medico.historia.sec@medconnect.com", null));
        Paciente paciente = pacienteRepository.guardar(new Paciente(null, "Pac Historia", "2", null, null, null, null, null, null));
        turnoRepository.guardar(new Turno(null, LocalDateTime.now(), "Clinica", medico, paciente, TurnoEstado.PENDIENTE));
        String pacienteId = String.valueOf(paciente.getId());

        mockMvc.perform(get("/api/historias-clinicas").param("pacienteId", pacienteId))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/historias-clinicas").param("pacienteId", pacienteId)
                        .cookie(jwtCookie(UsuarioRole.ADMINISTRADOR)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/historias-clinicas").param("pacienteId", pacienteId)
                        .cookie(jwtCookie(UsuarioRole.PACIENTE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/historias-clinicas").param("pacienteId", pacienteId)
                        .cookie(jwtCookie(UsuarioRole.MEDICO, medico.getEmail())))
                .andExpect(status().isOk());
    }

    @Test
    public void exportarHistoriaClinica_requiereRolAdministrador() throws Exception {
        mockMvc.perform(get("/api/historias-clinicas/exportar").param("pacienteId", "1"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/historias-clinicas/exportar").param("pacienteId", "1")
                        .cookie(jwtCookie(UsuarioRole.MEDICO)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/historias-clinicas/exportar").param("pacienteId", "1")
                        .cookie(jwtCookie(UsuarioRole.ADMINISTRADOR)))
                .andExpect(noRechazadoPorAutorizacion());
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
        public Optional<Medico> buscarPorId(Long id) {
            return store.stream().filter(m -> m.getId().equals(id)).findFirst();
        }

        @Override
        public List<Medico> buscarPorIds(List<Long> ids) {
            return store.stream().filter(m -> ids.contains(m.getId())).toList();
        }

        @Override
        public Optional<Medico> buscarPorEmail(String email) {
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
        public Optional<Paciente> buscarPorId(Long id) {
            return store.stream().filter(p -> p.getId().equals(id)).findFirst();
        }

        @Override
        public List<Paciente> buscarPorIds(List<Long> ids) {
            return store.stream().filter(p -> ids.contains(p.getId())).toList();
        }

        @Override
        public Optional<Paciente> buscarPorEmail(String email) {
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

    static class InMemoryTurnoRepository implements TurnoRepository {
        private final List<Turno> store = new ArrayList<>();
        private long seq = 1;

        @Override
        public Turno guardar(Turno turno) {
            // Upsert real: si ya tiene id (p.ej. ActualizarEstadoTurnoService
            // reguardando un turno existente) hay que reemplazarlo en la lista,
            // no asignarle un id nuevo -- si no, buscarPorId(idOriginal) deja
            // de encontrarlo despues de la primera actualizacion.
            if (turno.getId() == null) {
                turno.setId(seq++);
            } else {
                store.removeIf(t -> t.getId().equals(turno.getId()));
            }
            store.add(turno);
            return turno;
        }

        @Override
        public Optional<Turno> buscarPorId(Long id) {
            return store.stream().filter(t -> t.getId().equals(id)).findFirst();
        }

        @Override
        public List<Turno> buscarPorMedico(Long medicoId) {
            return store.stream().filter(t -> t.getMedico() != null && t.getMedico().getId().equals(medicoId)).toList();
        }

        @Override
        public List<Turno> buscarPorPaciente(Long pacienteId) {
            return store.stream().filter(t -> t.getPaciente() != null && t.getPaciente().getId().equals(pacienteId)).toList();
        }

        @Override
        public List<Turno> buscarTodos() {
            return new ArrayList<>(store);
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
        public Optional<Usuario> buscarPorEmail(String email) {
            return store.stream().filter(u -> u.getEmail().equals(email)).findFirst();
        }

        @Override
        public Optional<Usuario> buscarPorId(Long id) {
            return store.stream().filter(u -> u.getId().equals(id)).findFirst();
        }

        @Override
        public List<Usuario> buscarTodos() {
            return new ArrayList<>(store);
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
            return store.stream().filter(r -> r.getPaciente() != null && r.getPaciente().getId().equals(pacienteId)).toList();
        }
    }
}
