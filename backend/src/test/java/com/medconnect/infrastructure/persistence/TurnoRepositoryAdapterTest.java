package com.medconnect.infrastructure.persistence;

import com.medconnect.domain.exception.TurnoInvalidoException;
import com.medconnect.domain.model.Medico;
import com.medconnect.domain.model.Paciente;
import com.medconnect.domain.model.Turno;
import com.medconnect.domain.model.TurnoEstado;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class TurnoRepositoryAdapterTest {

    private Turno turno() {
        Medico medico = new Medico(2L, null, null, null, null, null, null);
        Paciente paciente = new Paciente(3L, null, null, null, null, null, null, null, null);
        return new Turno(null, LocalDateTime.of(2026, 8, 12, 10, 0), "Cardiología", medico, paciente, TurnoEstado.PENDIENTE);
    }

    @Test
    public void guardar_devuelveElTurnoConId_siLaEscrituraTieneExito() {
        TurnoJpaRepository jpaRepository = Mockito.mock(TurnoJpaRepository.class);
        TurnoEntity guardada = new TurnoEntity(10L, LocalDateTime.of(2026, 8, 12, 10, 0), "Cardiología", 2L, 3L, TurnoEstado.PENDIENTE, null);
        when(jpaRepository.save(any(TurnoEntity.class))).thenReturn(guardada);

        TurnoRepositoryAdapter adapter = new TurnoRepositoryAdapter(jpaRepository);

        Turno resultado = adapter.guardar(turno());

        assertEquals(10L, resultado.getId());
    }

    // MEDIUM de la re-auditoria e2e (2026-09-08): este try/catch vivia antes
    // en CrearTurnoService (application.usecase), que terminaba dependiendo
    // de un tipo de Spring Data (violacion de arquitectura hexagonal -- la
    // capa de aplicacion no deberia conocer excepciones de infraestructura).
    // Se movio aca, al unico lado con permiso de depender de ambos mundos.
    @Test
    public void guardar_traduceDataIntegrityViolationException_aTurnoInvalidoException() {
        TurnoJpaRepository jpaRepository = Mockito.mock(TurnoJpaRepository.class);
        // Simula dos requests concurrentes reservando el mismo horario con el
        // mismo medico: el chequeo en memoria de CrearTurnoService no lo
        // detecta, pero la unique constraint de la base lo rechaza.
        when(jpaRepository.save(any(TurnoEntity.class)))
                .thenThrow(new DataIntegrityViolationException("unique constraint"));

        TurnoRepositoryAdapter adapter = new TurnoRepositoryAdapter(jpaRepository);

        assertThrows(TurnoInvalidoException.class, () -> adapter.guardar(turno()));
    }
}
