package com.medconnect.application.usecase;

import com.medconnect.domain.exception.PacienteInvalidoException;
import com.medconnect.domain.model.Paciente;
import com.medconnect.domain.model.Turno;
import com.medconnect.domain.model.TurnoEstado;
import com.medconnect.domain.port.PacienteRepository;
import com.medconnect.domain.port.TurnoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class EliminarPacienteServiceTest {

    private final Paciente paciente = new Paciente(1L, "Juan Gómez", "30111222", null, null, null, null, null, null);

    private Turno turno(LocalDateTime fechaHora, TurnoEstado estado) {
        return new Turno(1L, fechaHora, "Cardiología", null, paciente, estado);
    }

    @Test
    public void eliminar_devuelveTrueYBorra_siExiste_yNoTieneTurnos() {
        PacienteRepository repo = Mockito.mock(PacienteRepository.class);
        TurnoRepository turnoRepo = Mockito.mock(TurnoRepository.class);
        when(repo.buscarPorId(1L)).thenReturn(Optional.of(paciente));
        when(turnoRepo.buscarPorPaciente(1L)).thenReturn(List.of());

        EliminarPacienteService service = new EliminarPacienteService(repo, turnoRepo);

        assertTrue(service.eliminar(1L));
        Mockito.verify(repo).eliminar(1L);
    }

    @Test
    public void eliminar_devuelveTrueYBorra_siSoloTieneTurnosPasadosOCancelados() {
        PacienteRepository repo = Mockito.mock(PacienteRepository.class);
        TurnoRepository turnoRepo = Mockito.mock(TurnoRepository.class);
        when(repo.buscarPorId(1L)).thenReturn(Optional.of(paciente));
        when(turnoRepo.buscarPorPaciente(1L)).thenReturn(List.of(
                turno(LocalDateTime.now().minusDays(1), TurnoEstado.CONFIRMADO),
                turno(LocalDateTime.now().plusDays(1), TurnoEstado.CANCELADO)
        ));

        EliminarPacienteService service = new EliminarPacienteService(repo, turnoRepo);

        assertTrue(service.eliminar(1L));
        Mockito.verify(repo).eliminar(1L);
    }

    @Test
    public void eliminar_tiraExcepcion_siTieneUnTurnoPendienteOConfirmadoAFuturo() {
        PacienteRepository repo = Mockito.mock(PacienteRepository.class);
        TurnoRepository turnoRepo = Mockito.mock(TurnoRepository.class);
        when(repo.buscarPorId(1L)).thenReturn(Optional.of(paciente));
        when(turnoRepo.buscarPorPaciente(1L)).thenReturn(List.of(
                turno(LocalDateTime.now().plusDays(1), TurnoEstado.PENDIENTE)
        ));

        EliminarPacienteService service = new EliminarPacienteService(repo, turnoRepo);

        assertThrows(PacienteInvalidoException.class, () -> service.eliminar(1L));
        Mockito.verify(repo, Mockito.never()).eliminar(Mockito.anyLong());
    }

    @Test
    public void eliminar_devuelveFalse_siNoExiste() {
        PacienteRepository repo = Mockito.mock(PacienteRepository.class);
        TurnoRepository turnoRepo = Mockito.mock(TurnoRepository.class);
        when(repo.buscarPorId(99L)).thenReturn(Optional.empty());

        EliminarPacienteService service = new EliminarPacienteService(repo, turnoRepo);

        assertFalse(service.eliminar(99L));
        Mockito.verify(repo, Mockito.never()).eliminar(Mockito.anyLong());
        Mockito.verifyNoInteractions(turnoRepo);
    }
}
