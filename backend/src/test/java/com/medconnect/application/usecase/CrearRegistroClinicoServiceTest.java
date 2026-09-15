package com.medconnect.application.usecase;

import com.medconnect.TestFixtures;
import com.medconnect.domain.exception.RegistroClinicoInvalidoException;
import com.medconnect.domain.model.RegistroClinico;
import com.medconnect.domain.model.Turno;
import com.medconnect.domain.model.TurnoEstado;
import com.medconnect.domain.port.MedicoRepository;
import com.medconnect.domain.port.PacienteRepository;
import com.medconnect.domain.port.RegistroClinicoRepository;
import com.medconnect.domain.port.TurnoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// LOW de la re-auditoria e2e (2026-09-08, segunda ronda): los mocks y el
// service se reconstruian desde cero en cada @Test -- boilerplate identico
// por clase. Movidos a campos + @BeforeEach.
public class CrearRegistroClinicoServiceTest {

    private RegistroClinicoRepository repo;
    private MedicoRepository medicoRepo;
    private PacienteRepository pacienteRepo;
    private TurnoRepository turnoRepo;
    private CrearRegistroClinicoService service;

    @BeforeEach
    public void setUp() {
        repo = Mockito.mock(RegistroClinicoRepository.class);
        medicoRepo = Mockito.mock(MedicoRepository.class);
        pacienteRepo = Mockito.mock(PacienteRepository.class);
        turnoRepo = Mockito.mock(TurnoRepository.class);
        service = new CrearRegistroClinicoService(repo, medicoRepo, pacienteRepo, turnoRepo);
    }

    private static CreateRegistroClinicoRequest requestValido() {
        return new CreateRegistroClinicoRequest(2L, 3L, "Fractura de tobillo", "Antibióticos por 7 días, reposo", "Control en 2 semanas");
    }

    // Turno ya ocurrido y no cancelado: el caso que efectivamente habilita
    // la historia clinica (ver Turno.habilitaHistoriaClinica).
    private static Turno turnoEntre(Long medicoId, Long pacienteId) {
        return turnoEntre(medicoId, pacienteId, LocalDateTime.now().minusDays(1), TurnoEstado.CONFIRMADO);
    }

    private static Turno turnoEntre(Long medicoId, Long pacienteId, LocalDateTime fechaHora, TurnoEstado estado) {
        return new Turno(1L, fechaHora, "Traumatología",
                TestFixtures.medicoConId(medicoId),
                TestFixtures.pacienteConId(pacienteId),
                estado);
    }

    @Test
    public void crear_guardaYDevuelveId_siElMedicoTieneUnTurnoConElPaciente() {
        when(medicoRepo.buscarPorId(2L)).thenReturn(Optional.of(TestFixtures.medicoConId(2L)));
        when(pacienteRepo.buscarPorId(3L)).thenReturn(Optional.of(TestFixtures.pacienteConId(3L)));
        when(turnoRepo.buscarPorMedico(2L)).thenReturn(List.of(turnoEntre(2L, 3L)));
        when(repo.guardar(any(RegistroClinico.class))).thenAnswer(invocation -> {
            RegistroClinico r = invocation.getArgument(0);
            r.setId(1L);
            return r;
        });

        CreateRegistroClinicoResponse resp = service.crear(requestValido());

        assertEquals(1L, resp.getId());
    }

    @Test
    public void crear_lanzaExcepcion_siElMedicoNoTieneNingunTurnoConElPaciente() {
        when(medicoRepo.buscarPorId(2L)).thenReturn(Optional.of(TestFixtures.medicoConId(2L)));
        when(pacienteRepo.buscarPorId(3L)).thenReturn(Optional.of(TestFixtures.pacienteConId(3L)));
        when(turnoRepo.buscarPorMedico(2L)).thenReturn(List.of(turnoEntre(2L, 99L)));

        assertThrows(RegistroClinicoInvalidoException.class, () -> service.crear(requestValido()));
        Mockito.verify(repo, Mockito.never()).guardar(any(RegistroClinico.class));
    }

    @Test
    public void crear_lanzaExcepcion_siMedicoNoExiste() {
        when(medicoRepo.buscarPorId(2L)).thenReturn(Optional.empty());

        assertThrows(RegistroClinicoInvalidoException.class, () -> service.crear(requestValido()));
    }

    @Test
    public void crear_lanzaExcepcion_siPacienteNoExiste() {
        when(medicoRepo.buscarPorId(2L)).thenReturn(Optional.of(TestFixtures.medicoConId(2L)));
        when(pacienteRepo.buscarPorId(3L)).thenReturn(Optional.empty());

        assertThrows(RegistroClinicoInvalidoException.class, () -> service.crear(requestValido()));
    }

    @Test
    public void crear_lanzaExcepcion_siElUnicoTurnoConElPacienteEstaCancelado() {
        when(medicoRepo.buscarPorId(2L)).thenReturn(Optional.of(TestFixtures.medicoConId(2L)));
        when(pacienteRepo.buscarPorId(3L)).thenReturn(Optional.of(TestFixtures.pacienteConId(3L)));
        when(turnoRepo.buscarPorMedico(2L)).thenReturn(List.of(
                turnoEntre(2L, 3L, LocalDateTime.now().minusDays(1), TurnoEstado.CANCELADO)));

        assertThrows(RegistroClinicoInvalidoException.class, () -> service.crear(requestValido()));
        Mockito.verify(repo, Mockito.never()).guardar(any(RegistroClinico.class));
    }

    @Test
    public void crear_lanzaExcepcion_siElUnicoTurnoConElPacienteEsAFuturo() {
        when(medicoRepo.buscarPorId(2L)).thenReturn(Optional.of(TestFixtures.medicoConId(2L)));
        when(pacienteRepo.buscarPorId(3L)).thenReturn(Optional.of(TestFixtures.pacienteConId(3L)));
        when(turnoRepo.buscarPorMedico(2L)).thenReturn(List.of(
                turnoEntre(2L, 3L, LocalDateTime.now().plusDays(1), TurnoEstado.CONFIRMADO)));

        assertThrows(RegistroClinicoInvalidoException.class, () -> service.crear(requestValido()));
        Mockito.verify(repo, Mockito.never()).guardar(any(RegistroClinico.class));
    }

    @Test
    public void crear_lanzaExcepcion_siFaltaDiagnostico() {
        CreateRegistroClinicoRequest req = new CreateRegistroClinicoRequest(2L, 3L, "  ", "Antibióticos", null);

        assertThrows(RegistroClinicoInvalidoException.class, () -> service.crear(req));
    }

    @Test
    public void crear_lanzaExcepcion_siFaltaTratamiento() {
        CreateRegistroClinicoRequest req = new CreateRegistroClinicoRequest(2L, 3L, "Fractura", null, null);

        assertThrows(RegistroClinicoInvalidoException.class, () -> service.crear(req));
    }
}
