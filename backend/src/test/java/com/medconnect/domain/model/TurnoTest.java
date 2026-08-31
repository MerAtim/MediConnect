package com.medconnect.domain.model;

import com.medconnect.domain.exception.TurnoInvalidoException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TurnoTest {

    private Turno turno(TurnoEstado estado) {
        return new Turno(1L, LocalDateTime.of(2026, 8, 12, 10, 0), "Cardiología", null, null, estado);
    }

    @Test
    public void cambiarEstado_actualizaElEstado_siElTurnoNoEstaCancelado() {
        Turno turno = turno(TurnoEstado.PENDIENTE);

        turno.cambiarEstado(TurnoEstado.CONFIRMADO);

        assertEquals(TurnoEstado.CONFIRMADO, turno.getEstado());
    }

    @Test
    public void cambiarEstado_lanzaExcepcion_siElTurnoYaEstaCancelado() {
        Turno turno = turno(TurnoEstado.CANCELADO);

        assertThrows(TurnoInvalidoException.class, () -> turno.cambiarEstado(TurnoEstado.CONFIRMADO));
        // La invariante se cumple pase lo que pase por afuera: no existe otro
        // metodo publico para mutar el estado sin pasar por este chequeo.
        assertEquals(TurnoEstado.CANCELADO, turno.getEstado());
    }

    @Test
    public void cambiarEstado_permiteCancelarUnTurnoPendiente() {
        Turno turno = turno(TurnoEstado.PENDIENTE);

        turno.cambiarEstado(TurnoEstado.CANCELADO);

        assertEquals(TurnoEstado.CANCELADO, turno.getEstado());
    }
}
