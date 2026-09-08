package com.medconnect.application.usecase;

import com.medconnect.domain.exception.MedicoInvalidoException;
import com.medconnect.domain.model.Medico;
import com.medconnect.domain.model.Turno;
import com.medconnect.domain.model.TurnoEstado;
import com.medconnect.domain.port.MedicoRepository;
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

public class EliminarMedicoServiceTest {

    private final Medico medico = new Medico(1L, "Ana Pérez", "Cardiología", "MP1234", null, null, null, null);

    private Turno turno(LocalDateTime fechaHora, TurnoEstado estado) {
        return new Turno(1L, fechaHora, "Cardiología", medico, null, estado);
    }

    @Test
    public void eliminar_devuelveTrueYBorra_siExiste_yNoTieneTurnos() {
        MedicoRepository repo = Mockito.mock(MedicoRepository.class);
        TurnoRepository turnoRepo = Mockito.mock(TurnoRepository.class);
        when(repo.buscarPorId(1L)).thenReturn(Optional.of(medico));
        when(turnoRepo.buscarPorMedico(1L)).thenReturn(List.of());

        EliminarMedicoService service = new EliminarMedicoService(repo, turnoRepo);

        assertTrue(service.eliminar(1L));
        Mockito.verify(repo).eliminar(1L);
    }

    @Test
    public void eliminar_devuelveTrueYBorra_siSoloTieneTurnosPasadosOCancelados() {
        MedicoRepository repo = Mockito.mock(MedicoRepository.class);
        TurnoRepository turnoRepo = Mockito.mock(TurnoRepository.class);
        when(repo.buscarPorId(1L)).thenReturn(Optional.of(medico));
        when(turnoRepo.buscarPorMedico(1L)).thenReturn(List.of(
                turno(LocalDateTime.now().minusDays(1), TurnoEstado.CONFIRMADO),
                turno(LocalDateTime.now().plusDays(1), TurnoEstado.CANCELADO)
        ));

        EliminarMedicoService service = new EliminarMedicoService(repo, turnoRepo);

        assertTrue(service.eliminar(1L));
        Mockito.verify(repo).eliminar(1L);
    }

    @Test
    public void eliminar_tiraExcepcion_siTieneUnTurnoPendienteOConfirmadoAFuturo() {
        MedicoRepository repo = Mockito.mock(MedicoRepository.class);
        TurnoRepository turnoRepo = Mockito.mock(TurnoRepository.class);
        when(repo.buscarPorId(1L)).thenReturn(Optional.of(medico));
        when(turnoRepo.buscarPorMedico(1L)).thenReturn(List.of(
                turno(LocalDateTime.now().plusDays(1), TurnoEstado.PENDIENTE)
        ));

        EliminarMedicoService service = new EliminarMedicoService(repo, turnoRepo);

        assertThrows(MedicoInvalidoException.class, () -> service.eliminar(1L));
        Mockito.verify(repo, Mockito.never()).eliminar(Mockito.anyLong());
    }

    @Test
    public void eliminar_devuelveFalse_siNoExiste() {
        MedicoRepository repo = Mockito.mock(MedicoRepository.class);
        TurnoRepository turnoRepo = Mockito.mock(TurnoRepository.class);
        when(repo.buscarPorId(99L)).thenReturn(Optional.empty());

        EliminarMedicoService service = new EliminarMedicoService(repo, turnoRepo);

        assertFalse(service.eliminar(99L));
        Mockito.verify(repo, Mockito.never()).eliminar(Mockito.anyLong());
        Mockito.verifyNoInteractions(turnoRepo);
    }
}
