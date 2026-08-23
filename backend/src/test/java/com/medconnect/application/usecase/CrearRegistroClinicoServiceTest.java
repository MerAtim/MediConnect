package com.medconnect.application.usecase;

import com.medconnect.domain.exception.RegistroClinicoInvalidoException;
import com.medconnect.domain.model.Medico;
import com.medconnect.domain.model.Paciente;
import com.medconnect.domain.model.RegistroClinico;
import com.medconnect.domain.model.Turno;
import com.medconnect.domain.port.MedicoRepository;
import com.medconnect.domain.port.PacienteRepository;
import com.medconnect.domain.port.RegistroClinicoRepository;
import com.medconnect.domain.port.TurnoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class CrearRegistroClinicoServiceTest {

    private static CreateRegistroClinicoRequest requestValido() {
        return new CreateRegistroClinicoRequest(2L, 3L, "Fractura de tobillo", "Antibióticos por 7 días, reposo", "Control en 2 semanas");
    }

    private static Turno turnoEntre(Long medicoId, Long pacienteId) {
        return new Turno(1L, null, "Traumatología",
                new Medico(medicoId, null, null, null, null, null, null, null),
                new Paciente(pacienteId, null, null, null, null, null, null, null, null),
                null);
    }

    @Test
    public void crear_guardaYDevuelveId_siElMedicoTieneUnTurnoConElPaciente() {
        RegistroClinicoRepository repo = Mockito.mock(RegistroClinicoRepository.class);
        MedicoRepository medicoRepo = Mockito.mock(MedicoRepository.class);
        PacienteRepository pacienteRepo = Mockito.mock(PacienteRepository.class);
        TurnoRepository turnoRepo = Mockito.mock(TurnoRepository.class);

        when(medicoRepo.buscarPorId(2L)).thenReturn(Optional.of(new Medico(2L, null, null, null, null, null, null, null)));
        when(pacienteRepo.buscarPorId(3L)).thenReturn(Optional.of(new Paciente(3L, null, null, null, null, null, null, null, null)));
        when(turnoRepo.buscarPorMedico(2L)).thenReturn(List.of(turnoEntre(2L, 3L)));
        when(repo.guardar(any(RegistroClinico.class))).thenAnswer(invocation -> {
            RegistroClinico r = invocation.getArgument(0);
            r.setId(1L);
            return r;
        });

        CrearRegistroClinicoService service = new CrearRegistroClinicoService(repo, medicoRepo, pacienteRepo, turnoRepo);

        CreateRegistroClinicoResponse resp = service.crear(requestValido());

        assertEquals(1L, resp.getId());
    }

    @Test
    public void crear_lanzaExcepcion_siElMedicoNoTieneNingunTurnoConElPaciente() {
        RegistroClinicoRepository repo = Mockito.mock(RegistroClinicoRepository.class);
        MedicoRepository medicoRepo = Mockito.mock(MedicoRepository.class);
        PacienteRepository pacienteRepo = Mockito.mock(PacienteRepository.class);
        TurnoRepository turnoRepo = Mockito.mock(TurnoRepository.class);

        when(medicoRepo.buscarPorId(2L)).thenReturn(Optional.of(new Medico(2L, null, null, null, null, null, null, null)));
        when(pacienteRepo.buscarPorId(3L)).thenReturn(Optional.of(new Paciente(3L, null, null, null, null, null, null, null, null)));
        when(turnoRepo.buscarPorMedico(2L)).thenReturn(List.of(turnoEntre(2L, 99L)));

        CrearRegistroClinicoService service = new CrearRegistroClinicoService(repo, medicoRepo, pacienteRepo, turnoRepo);

        assertThrows(RegistroClinicoInvalidoException.class, () -> service.crear(requestValido()));
        Mockito.verify(repo, Mockito.never()).guardar(any(RegistroClinico.class));
    }

    @Test
    public void crear_lanzaExcepcion_siMedicoNoExiste() {
        RegistroClinicoRepository repo = Mockito.mock(RegistroClinicoRepository.class);
        MedicoRepository medicoRepo = Mockito.mock(MedicoRepository.class);
        PacienteRepository pacienteRepo = Mockito.mock(PacienteRepository.class);
        TurnoRepository turnoRepo = Mockito.mock(TurnoRepository.class);

        when(medicoRepo.buscarPorId(2L)).thenReturn(Optional.empty());

        CrearRegistroClinicoService service = new CrearRegistroClinicoService(repo, medicoRepo, pacienteRepo, turnoRepo);

        assertThrows(RegistroClinicoInvalidoException.class, () -> service.crear(requestValido()));
    }

    @Test
    public void crear_lanzaExcepcion_siPacienteNoExiste() {
        RegistroClinicoRepository repo = Mockito.mock(RegistroClinicoRepository.class);
        MedicoRepository medicoRepo = Mockito.mock(MedicoRepository.class);
        PacienteRepository pacienteRepo = Mockito.mock(PacienteRepository.class);
        TurnoRepository turnoRepo = Mockito.mock(TurnoRepository.class);

        when(medicoRepo.buscarPorId(2L)).thenReturn(Optional.of(new Medico(2L, null, null, null, null, null, null, null)));
        when(pacienteRepo.buscarPorId(3L)).thenReturn(Optional.empty());

        CrearRegistroClinicoService service = new CrearRegistroClinicoService(repo, medicoRepo, pacienteRepo, turnoRepo);

        assertThrows(RegistroClinicoInvalidoException.class, () -> service.crear(requestValido()));
    }

    @Test
    public void crear_lanzaExcepcion_siFaltaDiagnostico() {
        RegistroClinicoRepository repo = Mockito.mock(RegistroClinicoRepository.class);
        MedicoRepository medicoRepo = Mockito.mock(MedicoRepository.class);
        PacienteRepository pacienteRepo = Mockito.mock(PacienteRepository.class);
        TurnoRepository turnoRepo = Mockito.mock(TurnoRepository.class);

        CrearRegistroClinicoService service = new CrearRegistroClinicoService(repo, medicoRepo, pacienteRepo, turnoRepo);

        CreateRegistroClinicoRequest req = new CreateRegistroClinicoRequest(2L, 3L, "  ", "Antibióticos", null);

        assertThrows(RegistroClinicoInvalidoException.class, () -> service.crear(req));
    }

    @Test
    public void crear_lanzaExcepcion_siFaltaTratamiento() {
        RegistroClinicoRepository repo = Mockito.mock(RegistroClinicoRepository.class);
        MedicoRepository medicoRepo = Mockito.mock(MedicoRepository.class);
        PacienteRepository pacienteRepo = Mockito.mock(PacienteRepository.class);
        TurnoRepository turnoRepo = Mockito.mock(TurnoRepository.class);

        CrearRegistroClinicoService service = new CrearRegistroClinicoService(repo, medicoRepo, pacienteRepo, turnoRepo);

        CreateRegistroClinicoRequest req = new CreateRegistroClinicoRequest(2L, 3L, "Fractura", null, null);

        assertThrows(RegistroClinicoInvalidoException.class, () -> service.crear(req));
    }
}
