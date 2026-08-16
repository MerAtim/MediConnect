package com.medconnect.application.usecase;

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

    private static CreateTurnoRequest requestValido() {
        return new CreateTurnoRequest(
                LocalDateTime.of(2026, 8, 12, 10, 0),
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

        when(medicoRepo.buscarPorId(2L)).thenReturn(Optional.of(new Medico(2L, null, null, null, null, null, null, null)));
        when(pacienteRepo.buscarPorId(3L)).thenReturn(Optional.of(new Paciente(3L, null, null, null, null, null, null, null, null)));
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

        when(medicoRepo.buscarPorId(2L)).thenReturn(Optional.of(new Medico(2L, null, null, null, null, null, null, null)));
        when(pacienteRepo.buscarPorId(3L)).thenReturn(Optional.of(new Paciente(3L, null, null, null, null, null, null, null, null)));
        // Simular que ya existe un turno a la misma fecha para el médico
        when(repo.buscarPorMedico(2L)).thenReturn(java.util.List.of(
                new Turno(10L, LocalDateTime.of(2026, 8, 12, 10, 0), "Cardiología", null, null, null)
        ));

        CrearTurnoService service = new CrearTurnoService(repo, medicoRepo, pacienteRepo);

        assertThrows(RuntimeException.class, () -> service.crear(requestValido()));
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

        when(medicoRepo.buscarPorId(2L)).thenReturn(Optional.of(new Medico(2L, null, null, null, null, null, null, null)));
        when(pacienteRepo.buscarPorId(3L)).thenReturn(Optional.empty());

        CrearTurnoService service = new CrearTurnoService(repo, medicoRepo, pacienteRepo);

        assertThrows(TurnoInvalidoException.class, () -> service.crear(requestValido()));
    }
}
