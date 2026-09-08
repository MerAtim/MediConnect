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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @Autowired
    private UsuarioRepository usuarioRepository;

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
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nadie@medconnect.com\",\"contrasena\":\"x\"}"))
                // 401 por credenciales invalidas (CredencialesInvalidasException), nunca por
                // falta de autenticacion previa -- /api/v1/auth/** es permitAll.
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getMedicos_requiereRolAdministrador() throws Exception {
        mockMvc.perform(get("/api/v1/medicos"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/medicos").cookie(jwtCookie(UsuarioRole.MEDICO)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/medicos").cookie(jwtCookie(UsuarioRole.PACIENTE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/medicos").cookie(jwtCookie(UsuarioRole.ADMINISTRADOR)))
                .andExpect(status().isOk());
    }

    @Test
    public void postMedicos_requiereRolAdministrador() throws Exception {
        // HIGH de la re-auditoria e2e (2026-09-08): las 6 reglas de CRUD de
        // medico/paciente (POST/PUT/DELETE x 2) solo tenian cubierto el GET.
        String body = "{\"nombre\":\"Dr Post\",\"especialidad\":\"Clinica\",\"matricula\":\"MPOST-SEC-1\"}";
        mockMvc.perform(post("/api/v1/medicos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/medicos").contentType(MediaType.APPLICATION_JSON).content(body)
                        .cookie(jwtCookie(UsuarioRole.MEDICO)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/medicos").contentType(MediaType.APPLICATION_JSON).content(body)
                        .cookie(jwtCookie(UsuarioRole.PACIENTE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/medicos").contentType(MediaType.APPLICATION_JSON).content(body)
                        .cookie(jwtCookie(UsuarioRole.ADMINISTRADOR)))
                .andExpect(status().isCreated());
    }

    @Test
    public void putMedicos_requiereRolAdministrador() throws Exception {
        Medico medico = medicoRepository.guardar(new Medico(null, "Dr Put", "Clinica", "MPUT-SEC-1", null, null, null));
        String url = "/api/v1/medicos/" + medico.getId();
        String body = "{\"nombre\":\"Dr Put Editado\",\"especialidad\":\"Clinica\",\"matricula\":\"MPUT-SEC-1\"}";

        mockMvc.perform(put(url).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(put(url).contentType(MediaType.APPLICATION_JSON).content(body)
                        .cookie(jwtCookie(UsuarioRole.MEDICO)))
                .andExpect(status().isForbidden());
        mockMvc.perform(put(url).contentType(MediaType.APPLICATION_JSON).content(body)
                        .cookie(jwtCookie(UsuarioRole.PACIENTE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(put(url).contentType(MediaType.APPLICATION_JSON).content(body)
                        .cookie(jwtCookie(UsuarioRole.ADMINISTRADOR)))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteMedicos_requiereRolAdministrador() throws Exception {
        Medico medico = medicoRepository.guardar(new Medico(null, "Dr Delete", "Clinica", "MDEL-SEC-1", null, null, null));
        String url = "/api/v1/medicos/" + medico.getId();

        mockMvc.perform(delete(url)).andExpect(status().isForbidden());
        mockMvc.perform(delete(url).cookie(jwtCookie(UsuarioRole.MEDICO))).andExpect(status().isForbidden());
        mockMvc.perform(delete(url).cookie(jwtCookie(UsuarioRole.PACIENTE))).andExpect(status().isForbidden());
        mockMvc.perform(delete(url).cookie(jwtCookie(UsuarioRole.ADMINISTRADOR))).andExpect(status().isNoContent());
    }

    @Test
    public void getPacientes_permiteAdministradorYMedico_noPaciente() throws Exception {
        mockMvc.perform(get("/api/v1/pacientes"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/pacientes").cookie(jwtCookie(UsuarioRole.PACIENTE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/pacientes").cookie(jwtCookie(UsuarioRole.ADMINISTRADOR)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/pacientes").cookie(jwtCookie(UsuarioRole.MEDICO)))
                .andExpect(status().isOk());
    }

    @Test
    public void postPacientes_requiereRolAdministrador() throws Exception {
        String body = "{\"nombre\":\"Pac Post\",\"dni\":\"40111000\"}";
        mockMvc.perform(post("/api/v1/pacientes").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/pacientes").contentType(MediaType.APPLICATION_JSON).content(body)
                        .cookie(jwtCookie(UsuarioRole.MEDICO)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/pacientes").contentType(MediaType.APPLICATION_JSON).content(body)
                        .cookie(jwtCookie(UsuarioRole.PACIENTE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/pacientes").contentType(MediaType.APPLICATION_JSON).content(body)
                        .cookie(jwtCookie(UsuarioRole.ADMINISTRADOR)))
                .andExpect(status().isCreated());
    }

    @Test
    public void putPacientes_requiereRolAdministrador() throws Exception {
        Paciente paciente = pacienteRepository.guardar(new Paciente(null, "Pac Put", "40111001", null, null, null, null, null, null));
        String url = "/api/v1/pacientes/" + paciente.getId();
        String body = "{\"nombre\":\"Pac Put Editado\",\"dni\":\"40111001\"}";

        mockMvc.perform(put(url).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(put(url).contentType(MediaType.APPLICATION_JSON).content(body)
                        .cookie(jwtCookie(UsuarioRole.MEDICO)))
                .andExpect(status().isForbidden());
        mockMvc.perform(put(url).contentType(MediaType.APPLICATION_JSON).content(body)
                        .cookie(jwtCookie(UsuarioRole.PACIENTE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(put(url).contentType(MediaType.APPLICATION_JSON).content(body)
                        .cookie(jwtCookie(UsuarioRole.ADMINISTRADOR)))
                .andExpect(status().isOk());
    }

    @Test
    public void deletePacientes_requiereRolAdministrador() throws Exception {
        Paciente paciente = pacienteRepository.guardar(new Paciente(null, "Pac Delete", "40111002", null, null, null, null, null, null));
        String url = "/api/v1/pacientes/" + paciente.getId();

        mockMvc.perform(delete(url)).andExpect(status().isForbidden());
        mockMvc.perform(delete(url).cookie(jwtCookie(UsuarioRole.MEDICO))).andExpect(status().isForbidden());
        mockMvc.perform(delete(url).cookie(jwtCookie(UsuarioRole.PACIENTE))).andExpect(status().isForbidden());
        mockMvc.perform(delete(url).cookie(jwtCookie(UsuarioRole.ADMINISTRADOR))).andExpect(status().isNoContent());
    }

    @Test
    public void getPacientesEmailsVinculados_requiereRolAdministrador() throws Exception {
        mockMvc.perform(get("/api/v1/pacientes/emails-vinculados"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/pacientes/emails-vinculados").cookie(jwtCookie(UsuarioRole.MEDICO)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/pacientes/emails-vinculados").cookie(jwtCookie(UsuarioRole.PACIENTE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/pacientes/emails-vinculados").cookie(jwtCookie(UsuarioRole.ADMINISTRADOR)))
                .andExpect(status().isOk());
    }

    @Test
    public void getMedicoMe_requiereRolMedico() throws Exception {
        mockMvc.perform(get("/api/v1/medicos/me"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/medicos/me").cookie(jwtCookie(UsuarioRole.ADMINISTRADOR)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/medicos/me").cookie(jwtCookie(UsuarioRole.PACIENTE)))
                .andExpect(status().isForbidden());
        // Cuenta MEDICO sin un Medico vinculado -> pasa la autorizacion, el
        // controller da 404 el mismo, no 401/403. Lo que importa aca es que
        // el rol correcto no quede bloqueado por SecurityConfig.
        mockMvc.perform(get("/api/v1/medicos/me").cookie(jwtCookie(UsuarioRole.MEDICO)))
                .andExpect(noRechazadoPorAutorizacion());
    }

    @Test
    public void getPacienteMe_requiereRolPaciente() throws Exception {
        mockMvc.perform(get("/api/v1/pacientes/me"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/pacientes/me").cookie(jwtCookie(UsuarioRole.ADMINISTRADOR)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/pacientes/me").cookie(jwtCookie(UsuarioRole.MEDICO)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/pacientes/me").cookie(jwtCookie(UsuarioRole.PACIENTE)))
                .andExpect(noRechazadoPorAutorizacion());
    }

    @Test
    public void getUsuarios_requiereRolAdministrador() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/usuarios").cookie(jwtCookie(UsuarioRole.MEDICO)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/usuarios").cookie(jwtCookie(UsuarioRole.PACIENTE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/usuarios").cookie(jwtCookie(UsuarioRole.ADMINISTRADOR)))
                .andExpect(status().isOk());
    }

    @Test
    public void postUsuarios_requiereRolAdministrador() throws Exception {
        String body = "{\"nombre\":\"X\",\"email\":\"x@medconnect.com\",\"contrasena\":\"secreto123\",\"role\":\"PACIENTE\"}";
        mockMvc.perform(post("/api/v1/usuarios").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/usuarios").contentType(MediaType.APPLICATION_JSON).content(body)
                        .cookie(jwtCookie(UsuarioRole.MEDICO)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/usuarios").contentType(MediaType.APPLICATION_JSON).content(body)
                        .cookie(jwtCookie(UsuarioRole.ADMINISTRADOR)))
                .andExpect(status().isCreated());
    }

    @Test
    public void patchResetearContrasena_requiereRolAdministrador() throws Exception {
        // CRITICAL de la re-auditoria e2e (2026-09-08): esta regla (solo
        // ADMINISTRADOR puede resetear la contrasena de OTRO usuario, sin
        // conocer la actual) no tenia ningun test contra la cadena real de
        // seguridad -- si se afloja por error, cualquier autenticado podria
        // tomar control de cualquier cuenta, incluida una de ADMINISTRADOR.
        Usuario objetivo = usuarioRepository.guardar(
                new Usuario(null, "Usuario Objetivo", "objetivo.reset.sec@medconnect.com", "hashViejo", UsuarioRole.MEDICO));
        String url = "/api/v1/usuarios/" + objetivo.getId() + "/contrasena";
        String body = "{\"contrasenaNueva\":\"nuevaClave123\"}";

        mockMvc.perform(patch(url).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch(url).contentType(MediaType.APPLICATION_JSON).content(body)
                        .cookie(jwtCookie(UsuarioRole.MEDICO)))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch(url).contentType(MediaType.APPLICATION_JSON).content(body)
                        .cookie(jwtCookie(UsuarioRole.PACIENTE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch(url).contentType(MediaType.APPLICATION_JSON).content(body)
                        .cookie(jwtCookie(UsuarioRole.ADMINISTRADOR)))
                .andExpect(status().isNoContent());
    }

    @Test
    public void postTurnos_requiereRolAdministrador() throws Exception {
        String body = "{\"fechaHora\":\"2026-09-01T10:00:00\",\"especialidad\":\"Clinica\",\"medicoId\":1,\"pacienteId\":1}";
        mockMvc.perform(post("/api/v1/turnos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/turnos").contentType(MediaType.APPLICATION_JSON).content(body)
                        .cookie(jwtCookie(UsuarioRole.MEDICO)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/turnos").contentType(MediaType.APPLICATION_JSON).content(body)
                        .cookie(jwtCookie(UsuarioRole.ADMINISTRADOR)))
                .andExpect(noRechazadoPorAutorizacion());
    }

    @Test
    public void patchEstadoTurno_permiteCualquierRolAutenticado() throws Exception {
        // Ademas del rol, el endpoint exige pertenencia para MEDICO/PACIENTE:
        // sembramos un turno real para que "no rechazado por autorizacion"
        // pruebe el camino completo, no solo la regla de SecurityConfig.
        Medico medico = medicoRepository.guardar(new Medico(null, "Dr Turno", "Clinica", "MTUR-1", null, null, "medico.turno.sec@medconnect.com"));
        Paciente paciente = pacienteRepository.guardar(new Paciente(null, "Pac Turno", "1", null, null, null, null, null, "paciente.turno.sec@medconnect.com"));
        Turno turno = turnoRepository.guardar(new Turno(null, LocalDateTime.now(), "Clinica", medico, paciente, TurnoEstado.PENDIENTE));
        String url = "/api/v1/turnos/" + turno.getId() + "/estado";

        mockMvc.perform(patch(url).contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"CONFIRMADO\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch(url).contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"CONFIRMADO\"}")
                        .cookie(jwtCookie(UsuarioRole.ADMINISTRADOR)))
                .andExpect(noRechazadoPorAutorizacion());
        mockMvc.perform(patch(url).contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"CONFIRMADO\"}")
                        .cookie(jwtCookie(UsuarioRole.MEDICO, medico.getEmail().getValor())))
                .andExpect(noRechazadoPorAutorizacion());
        mockMvc.perform(patch(url).contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"CANCELADO\"}")
                        .cookie(jwtCookie(UsuarioRole.PACIENTE, paciente.getEmail().getValor())))
                .andExpect(noRechazadoPorAutorizacion());
    }

    @Test
    public void getHistoriasClinicas_requiereRolMedico() throws Exception {
        // esPacienteDeEseMedico exige un turno real entre ambos para dejar
        // pasar a un MEDICO -- lo sembramos para que el caso permitido de
        // este test pruebe el camino completo (rol + pertenencia), no solo
        // que la regla de rol no lo bloquee.
        Medico medico = medicoRepository.guardar(new Medico(null, "Dr Historia", "Clinica", "MHIST-1", null, null, "medico.historia.sec@medconnect.com"));
        Paciente paciente = pacienteRepository.guardar(new Paciente(null, "Pac Historia", "2", null, null, null, null, null, null));
        turnoRepository.guardar(new Turno(null, LocalDateTime.now(), "Clinica", medico, paciente, TurnoEstado.PENDIENTE));
        String pacienteId = String.valueOf(paciente.getId());

        mockMvc.perform(get("/api/v1/historias-clinicas").param("pacienteId", pacienteId))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/historias-clinicas").param("pacienteId", pacienteId)
                        .cookie(jwtCookie(UsuarioRole.ADMINISTRADOR)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/historias-clinicas").param("pacienteId", pacienteId)
                        .cookie(jwtCookie(UsuarioRole.PACIENTE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/historias-clinicas").param("pacienteId", pacienteId)
                        .cookie(jwtCookie(UsuarioRole.MEDICO, medico.getEmail().getValor())))
                .andExpect(status().isOk());
    }

    @Test
    public void postHistoriasClinicas_requiereRolMedico() throws Exception {
        // CRITICAL de la re-auditoria e2e (2026-09-08): solo la lectura
        // (GET) de historias clinicas tenia test contra la cadena real de
        // seguridad; la escritura (POST, contenido de PHI) no tenia ninguno.
        Medico medico = medicoRepository.guardar(
                new Medico(null, "Dr Historia Post", "Clinica", "MHPOST-1", null, null, "medico.historia.post.sec@medconnect.com"));
        Paciente paciente = pacienteRepository.guardar(new Paciente(null, "Pac Historia Post", "3", null, null, null, null, null, null));
        turnoRepository.guardar(new Turno(null, LocalDateTime.now(), "Clinica", medico, paciente, TurnoEstado.PENDIENTE));
        String body = "{\"pacienteId\":" + paciente.getId() + ",\"diagnostico\":\"dx\",\"tratamiento\":\"tx\"}";

        mockMvc.perform(post("/api/v1/historias-clinicas").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/historias-clinicas").contentType(MediaType.APPLICATION_JSON).content(body)
                        .cookie(jwtCookie(UsuarioRole.ADMINISTRADOR)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/historias-clinicas").contentType(MediaType.APPLICATION_JSON).content(body)
                        .cookie(jwtCookie(UsuarioRole.PACIENTE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/historias-clinicas").contentType(MediaType.APPLICATION_JSON).content(body)
                        .cookie(jwtCookie(UsuarioRole.MEDICO, medico.getEmail().getValor())))
                .andExpect(status().isCreated());
    }

    @Test
    public void exportarHistoriaClinica_requiereRolAdministrador() throws Exception {
        mockMvc.perform(get("/api/v1/historias-clinicas/exportar").param("pacienteId", "1"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/historias-clinicas/exportar").param("pacienteId", "1")
                        .cookie(jwtCookie(UsuarioRole.MEDICO)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/historias-clinicas/exportar").param("pacienteId", "1")
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
            return store.stream().filter(m -> m.getEmail() != null && email.equals(m.getEmail().getValor())).findFirst();
        }

        @Override
        public boolean existeEmailEnPerfilEliminado(String email) {
            // Este fake hace hard-delete (ver eliminar() abajo), asi que nunca
            // hay una fila "eliminada pero presente" para encontrar.
            return false;
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
            return store.stream().filter(p -> p.getEmail() != null && email.equals(p.getEmail().getValor())).findFirst();
        }

        @Override
        public boolean existeEmailEnPerfilEliminado(String email) {
            // Este fake hace hard-delete (ver eliminar() abajo), asi que nunca
            // hay una fila "eliminada pero presente" para encontrar.
            return false;
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
            return store.stream().filter(u -> u.getEmail().getValor().equals(email)).findFirst();
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
