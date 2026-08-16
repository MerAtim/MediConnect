package com.medconnect.application.usecase;

import com.medconnect.domain.exception.TurnoInvalidoException;
import com.medconnect.domain.model.Turno;
import com.medconnect.domain.model.TurnoEstado;
import com.medconnect.domain.port.TurnoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ActualizarEstadoTurnoServiceTest {

    @Test
    public void actualizarEstado_cambiaEstadoYGuarda_siTurnoExiste() {
        TurnoRepository repo = Mockito.mock(TurnoRepository.class);
        Turno turno = new Turno(1L, LocalDateTime.of(2026, 8, 12, 10, 0), "Cardiología", null, null, TurnoEstado.PENDIENTE);

        when(repo.buscarPorId(1L)).thenReturn(Optional.of(turno));
        when(repo.guardar(any(Turno.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ActualizarEstadoTurnoService service = new ActualizarEstadoTurnoService(repo);

        Optional<Turno> resultado = service.actualizarEstado(1L, TurnoEstado.CONFIRMADO);

        assertTrue(resultado.isPresent());
        assertEquals(TurnoEstado.CONFIRMADO, resultado.get().getEstado());
    }

    @Test
    public void actualizarEstado_devuelveVacio_siTurnoNoExiste() {
        TurnoRepository repo = Mockito.mock(TurnoRepository.class);
        when(repo.buscarPorId(99L)).thenReturn(Optional.empty());

        ActualizarEstadoTurnoService service = new ActualizarEstadoTurnoService(repo);

        assertTrue(service.actualizarEstado(99L, TurnoEstado.CONFIRMADO).isEmpty());
    }

    @Test
    public void actualizarEstado_lanzaExcepcion_siTurnoYaEstaCancelado() {
        TurnoRepository repo = Mockito.mock(TurnoRepository.class);
        Turno turno = new Turno(1L, LocalDateTime.of(2026, 8, 12, 10, 0), "Cardiología", null, null, TurnoEstado.CANCELADO);
        when(repo.buscarPorId(1L)).thenReturn(Optional.of(turno));

        ActualizarEstadoTurnoService service = new ActualizarEstadoTurnoService(repo);

        assertThrows(TurnoInvalidoException.class, () -> service.actualizarEstado(1L, TurnoEstado.CONFIRMADO));
    }
}
