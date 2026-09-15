package com.medconnect.infrastructure.persistence;

import com.medconnect.TestFixtures;
import com.medconnect.domain.exception.TurnoInvalidoException;
import com.medconnect.domain.model.Medico;
import com.medconnect.domain.model.Paciente;
import com.medconnect.domain.model.Turno;
import com.medconnect.domain.model.TurnoEstado;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class TurnoRepositoryAdapterTest {

    private Turno turno() {
        Medico medico = TestFixtures.medicoConId(2L);
        Paciente paciente = TestFixtures.pacienteConId(3L);
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

    // LOW de la re-auditoria e2e (2026-09-08, segunda ronda): "paginacion
    // falsa" -- estos 6 tests verifican que las 3 consultas paginadas
    // (por medico, por paciente, todos) arman el PageRequest correcto y
    // devuelven el total real, no un subList en memoria de la tabla entera.
    @Test
    public void buscarPaginaPorMedico_delegaEnFindByMedicoIdConPageable() {
        TurnoJpaRepository jpaRepository = Mockito.mock(TurnoJpaRepository.class);
        TurnoEntity entity = new TurnoEntity(10L, LocalDateTime.of(2026, 8, 12, 10, 0), "Cardiología", 2L, 3L, TurnoEstado.PENDIENTE, null);
        when(jpaRepository.findByMedicoId(eq(2L), eq(PageRequest.of(1, 5))))
                .thenReturn(new PageImpl<>(List.of(entity)));

        TurnoRepositoryAdapter adapter = new TurnoRepositoryAdapter(jpaRepository);

        List<Turno> resultado = adapter.buscarPaginaPorMedico(2L, 1, 5);

        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).getId());
    }

    @Test
    public void contarPorMedico_delegaEnCountByMedicoId() {
        TurnoJpaRepository jpaRepository = Mockito.mock(TurnoJpaRepository.class);
        when(jpaRepository.countByMedicoId(2L)).thenReturn(7L);

        TurnoRepositoryAdapter adapter = new TurnoRepositoryAdapter(jpaRepository);

        assertEquals(7L, adapter.contarPorMedico(2L));
    }

    @Test
    public void buscarPaginaPorPaciente_delegaEnFindByPacienteIdConPageable() {
        TurnoJpaRepository jpaRepository = Mockito.mock(TurnoJpaRepository.class);
        TurnoEntity entity = new TurnoEntity(11L, LocalDateTime.of(2026, 8, 12, 10, 0), "Cardiología", 2L, 3L, TurnoEstado.PENDIENTE, null);
        when(jpaRepository.findByPacienteId(eq(3L), eq(PageRequest.of(0, 20))))
                .thenReturn(new PageImpl<>(List.of(entity)));

        TurnoRepositoryAdapter adapter = new TurnoRepositoryAdapter(jpaRepository);

        List<Turno> resultado = adapter.buscarPaginaPorPaciente(3L, 0, 20);

        assertEquals(1, resultado.size());
        assertEquals(11L, resultado.get(0).getId());
    }

    @Test
    public void contarPorPaciente_delegaEnCountByPacienteId() {
        TurnoJpaRepository jpaRepository = Mockito.mock(TurnoJpaRepository.class);
        when(jpaRepository.countByPacienteId(3L)).thenReturn(4L);

        TurnoRepositoryAdapter adapter = new TurnoRepositoryAdapter(jpaRepository);

        assertEquals(4L, adapter.contarPorPaciente(3L));
    }

    @Test
    public void buscarPagina_clampeaPageYSizeNegativos_antesDeArmarElPageRequest() {
        TurnoJpaRepository jpaRepository = Mockito.mock(TurnoJpaRepository.class);
        // page=-1/size=0 no son validos para PageRequest.of (tira
        // IllegalArgumentException) -- el adapter los clampea a 0/1 antes,
        // mismo criterio que PageResponse.of ya usaba para el caso en memoria.
        when(jpaRepository.findAll(eq(PageRequest.of(0, 1)))).thenReturn(new PageImpl<>(List.of()));

        TurnoRepositoryAdapter adapter = new TurnoRepositoryAdapter(jpaRepository);

        List<Turno> resultado = adapter.buscarPagina(-1, 0);

        assertEquals(0, resultado.size());
    }

    @Test
    public void contar_delegaEnCount() {
        TurnoJpaRepository jpaRepository = Mockito.mock(TurnoJpaRepository.class);
        when(jpaRepository.count()).thenReturn(42L);

        TurnoRepositoryAdapter adapter = new TurnoRepositoryAdapter(jpaRepository);

        assertEquals(42L, adapter.contar());
    }
}
