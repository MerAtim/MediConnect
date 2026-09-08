package com.medconnect.domain.model;

import com.medconnect.domain.exception.TurnoInvalidoException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    public void habilitaHistoriaClinica_esTrue_siYaOcurrioYNoEstaCancelado() {
        Turno turno = turno(TurnoEstado.CONFIRMADO);

        assertTrue(turno.habilitaHistoriaClinica(LocalDateTime.of(2026, 8, 12, 10, 0).plusMinutes(1)));
    }

    @Test
    public void habilitaHistoriaClinica_esFalse_siElTurnoEstaCancelado() {
        Turno turno = turno(TurnoEstado.CANCELADO);

        assertFalse(turno.habilitaHistoriaClinica(LocalDateTime.of(2026, 8, 12, 10, 0).plusMinutes(1)));
    }

    @Test
    public void habilitaHistoriaClinica_esFalse_siElTurnoTodaviaNoOcurrio() {
        Turno turno = turno(TurnoEstado.CONFIRMADO);

        assertFalse(turno.habilitaHistoriaClinica(LocalDateTime.of(2026, 8, 12, 10, 0).minusMinutes(1)));
    }

    @Test
    public void habilitaHistoriaClinica_esTrue_enElMismoInstanteDelTurno() {
        Turno turno = turno(TurnoEstado.PENDIENTE);

        assertTrue(turno.habilitaHistoriaClinica(LocalDateTime.of(2026, 8, 12, 10, 0)));
    }

    @Test
    public void esFuturoActivo_esTrue_siEsPendienteOConfirmadoYAunNoOcurrio() {
        Turno turno = turno(TurnoEstado.PENDIENTE);

        assertTrue(turno.esFuturoActivo(LocalDateTime.of(2026, 8, 12, 10, 0).minusMinutes(1)));
    }

    @Test
    public void esFuturoActivo_esFalse_siEstaCancelado_aunqueSeaAFuturo() {
        Turno turno = turno(TurnoEstado.CANCELADO);

        assertFalse(turno.esFuturoActivo(LocalDateTime.of(2026, 8, 12, 10, 0).minusMinutes(1)));
    }

    @Test
    public void esFuturoActivo_esFalse_siYaOcurrio() {
        Turno turno = turno(TurnoEstado.CONFIRMADO);

        assertFalse(turno.esFuturoActivo(LocalDateTime.of(2026, 8, 12, 10, 0).plusMinutes(1)));
    }

    @Test
    public void esFuturoActivo_esFalse_enElMismoInstanteDelTurno() {
        Turno turno = turno(TurnoEstado.PENDIENTE);

        assertFalse(turno.esFuturoActivo(LocalDateTime.of(2026, 8, 12, 10, 0)));
    }
}
