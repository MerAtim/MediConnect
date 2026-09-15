package com.medconnect.infrastructure.persistence;

import com.medconnect.domain.model.Medico;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

// LOW de la re-auditoria e2e (2026-09-08, segunda ronda): "paginacion falsa"
// -- buscarPagina/contar reemplazan findAllActivos() + filtro/recorte en
// memoria por una consulta SQL con LIMIT/OFFSET y el filtro de especialidad
// en la misma consulta (":especialidad IS NULL OR ..."). Estos tests cubren
// la normalizacion null/blank -> null que hace el adapter antes de llamar
// al JPA repository.
public class MedicoRepositoryAdapterTest {

    @Test
    public void buscarPagina_pasaLaEspecialidadTalCual_siNoEsBlanca() {
        MedicoJpaRepository jpaRepository = Mockito.mock(MedicoJpaRepository.class);
        MedicoEntity entity = new MedicoEntity(1L, "Ana Pérez", "Cardiología", "MP1234", null, null, null);
        when(jpaRepository.findAllActivos(eq("Cardiología"), eq(PageRequest.of(0, 20))))
                .thenReturn(new PageImpl<>(List.of(entity)));

        MedicoRepositoryAdapter adapter = new MedicoRepositoryAdapter(jpaRepository);

        List<Medico> resultado = adapter.buscarPagina("Cardiología", 0, 20);

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
    }

    @Test
    public void buscarPagina_normalizaEspecialidadNull_aNull() {
        MedicoJpaRepository jpaRepository = Mockito.mock(MedicoJpaRepository.class);
        when(jpaRepository.findAllActivos(isNull(), eq(PageRequest.of(0, 20))))
                .thenReturn(new PageImpl<>(List.of()));

        MedicoRepositoryAdapter adapter = new MedicoRepositoryAdapter(jpaRepository);

        List<Medico> resultado = adapter.buscarPagina(null, 0, 20);

        assertEquals(0, resultado.size());
    }

    @Test
    public void buscarPagina_normalizaEspecialidadEnBlanco_aNull() {
        MedicoJpaRepository jpaRepository = Mockito.mock(MedicoJpaRepository.class);
        when(jpaRepository.findAllActivos(isNull(), eq(PageRequest.of(0, 20))))
                .thenReturn(new PageImpl<>(List.of()));

        MedicoRepositoryAdapter adapter = new MedicoRepositoryAdapter(jpaRepository);

        List<Medico> resultado = adapter.buscarPagina("   ", 0, 20);

        assertEquals(0, resultado.size());
    }

    @Test
    public void contar_normalizaEspecialidadEnBlanco_aNull() {
        MedicoJpaRepository jpaRepository = Mockito.mock(MedicoJpaRepository.class);
        when(jpaRepository.countActivos(isNull())).thenReturn(5L);

        MedicoRepositoryAdapter adapter = new MedicoRepositoryAdapter(jpaRepository);

        assertEquals(5L, adapter.contar(""));
    }
}
