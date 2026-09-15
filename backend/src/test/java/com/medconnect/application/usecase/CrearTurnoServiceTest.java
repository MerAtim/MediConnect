package com.medconnect.application.usecase;

import com.medconnect.TestFixtures;
import com.medconnect.domain.exception.TurnoInvalidoException;
import com.medconnect.domain.model.Medico;
import com.medconnect.domain.model.Paciente;
import com.medconnect.domain.model.Turno;
import com.medconnect.domain.port.MedicoRepository;
import com.medconnect.domain.port.PacienteRepository;
import com.medconnect.domain.port.TurnoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class CrearTurnoServiceTest {

    // Fecha fija en el pasado respecto de "ahora" pero usada solo para el
    // test que verifica el rechazo de fechas pasadas (LOW, segunda ronda de
    // re-auditoria: crear un turno con fecha pasada). El resto de los tests
    // usa FECHA_FUTURA para no quedar rotos con el correr del tiempo real.
    private static final LocalDateTime FECHA_FUTURA = LocalDateTime.now().plusDays(30).withNano(0);

    private static CreateTurnoRequest requestValido() {
        return new CreateTurnoRequest(
                FECHA_FUTURA,
                "Cardiología",
                2L,
                3L
        );
    }

    @Test
    public void crearTurno_guardaYDevuelveId() {
        TurnoRepository repo = Mockito.mock(TurnoRepository.class);
        MedicoRepository medicoRepo = Mockito.mock(MedicoRepository.class);
        PacienteRepository pacienteRepo = Mockito.mock(PacienteRepository.class);

        when(medicoRepo.buscarPorId(2L)).thenReturn(Optional.of(TestFixtures.medicoConId(2L)));
        when(pacienteRepo.buscarPorId(3L)).thenReturn(Optional.of(TestFixtures.pacienteConId(3L)));
        when(repo.guardar(any(Turno.class))).thenAnswer(invocation -> {
            Turno t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        CrearTurnoService service = new CrearTurnoService(repo, medicoRepo, pacienteRepo);

        CreateTurnoResponse resp = service.crear(requestValido());

        assertEquals(1L, resp.getId());
    }

    @Test
    public void crearTurno_lanzaExcepcion_siMedicoNoDisponible() {
        TurnoRepository repo = Mockito.mock(TurnoRepository.class);
        MedicoRepository medicoRepo = Mockito.mock(MedicoRepository.class);
        PacienteRepository pacienteRepo = Mockito.mock(PacienteRepository.class);

        when(medicoRepo.buscarPorId(2L)).thenReturn(Optional.of(TestFixtures.medicoConId(2L)));
        when(pacienteRepo.buscarPorId(3L)).thenReturn(Optional.of(TestFixtures.pacienteConId(3L)));
        // Simular que ya existe un turno a la misma fecha para el médico
        when(repo.buscarPorMedico(2L)).thenReturn(java.util.List.of(
                new Turno(10L, FECHA_FUTURA, "Cardiología", null, null, null)
        ));

        CrearTurnoService service = new CrearTurnoService(repo, medicoRepo, pacienteRepo);

        assertThrows(RuntimeException.class, () -> service.crear(requestValido()));
    }

    // MEDIUM de la re-auditoria e2e (2026-09-08): "doble reserva de
    // paciente sin test/decision" -- mismo mecanismo de deteccion que el
    // test de arriba, ahora del lado del paciente.
    @Test
    public void crearTurno_lanzaExcepcion_siPacienteYaTieneOtroTurnoEnEsaFechaHora() {
        TurnoRepository repo = Mockito.mock(TurnoRepository.class);
        MedicoRepository medicoRepo = Mockito.mock(MedicoRepository.class);
        PacienteRepository pacienteRepo = Mockito.mock(PacienteRepository.class);

        when(medicoRepo.buscarPorId(2L)).thenReturn(Optional.of(TestFixtures.medicoConId(2L)));
        when(pacienteRepo.buscarPorId(3L)).thenReturn(Optional.of(TestFixtures.pacienteConId(3L)));
        // El medico esta libre a esa hora, pero el paciente ya tiene otro
        // turno (con un medico distinto) en esa misma fechaHora.
        when(repo.buscarPorPaciente(3L)).thenReturn(java.util.List.of(
                new Turno(10L, FECHA_FUTURA, "Dermatología", null, null, null)
        ));

        CrearTurnoService service = new CrearTurnoService(repo, medicoRepo, pacienteRepo);

        assertThrows(TurnoInvalidoException.class, () -> service.crear(requestValido()));
    }

    @Test
    public void crearTurno_lanzaExcepcion_siMedicoNoExiste() {
        TurnoRepository repo = Mockito.mock(TurnoRepository.class);
        MedicoRepository medicoRepo = Mockito.mock(MedicoRepository.class);
        PacienteRepository pacienteRepo = Mockito.mock(PacienteRepository.class);

        when(medicoRepo.buscarPorId(2L)).thenReturn(Optional.empty());

        CrearTurnoService service = new CrearTurnoService(repo, medicoRepo, pacienteRepo);

        assertThrows(TurnoInvalidoException.class, () -> service.crear(requestValido()));
    }

    @Test
    public void crearTurno_lanzaExcepcion_siPacienteNoExiste() {
        TurnoRepository repo = Mockito.mock(TurnoRepository.class);
        MedicoRepository medicoRepo = Mockito.mock(MedicoRepository.class);
        PacienteRepository pacienteRepo = Mockito.mock(PacienteRepository.class);

        when(medicoRepo.buscarPorId(2L)).thenReturn(Optional.of(TestFixtures.medicoConId(2L)));
        when(pacienteRepo.buscarPorId(3L)).thenReturn(Optional.empty());

        CrearTurnoService service = new CrearTurnoService(repo, medicoRepo, pacienteRepo);

        assertThrows(TurnoInvalidoException.class, () -> service.crear(requestValido()));
    }

    // LOW de la re-auditoria e2e (2026-09-08, segunda ronda): "CrearTurnoService
    // no valida que fechaHora sea futura" -- un turno creado con fecha pasada
    // satisfacia de inmediato Turno.habilitaHistoriaClinica().
    @Test
    public void crearTurno_lanzaExcepcion_siFechaHoraEsPasada() {
        TurnoRepository repo = Mockito.mock(TurnoRepository.class);
        MedicoRepository medicoRepo = Mockito.mock(MedicoRepository.class);
        PacienteRepository pacienteRepo = Mockito.mock(PacienteRepository.class);

        CreateTurnoRequest requestConFechaPasada = new CreateTurnoRequest(
                LocalDateTime.now().minusDays(1), "Cardiología", 2L, 3L);

        CrearTurnoService service = new CrearTurnoService(repo, medicoRepo, pacienteRepo);

        assertThrows(TurnoInvalidoException.class, () -> service.crear(requestConFechaPasada));
    }
}
