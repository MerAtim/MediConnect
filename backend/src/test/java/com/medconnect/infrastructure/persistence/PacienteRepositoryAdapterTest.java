package com.medconnect.infrastructure.persistence;

import com.medconnect.domain.model.Paciente;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

// LOW de la re-auditoria e2e (2026-09-08, segunda ronda): "paginacion falsa"
// -- buscarPagina/contar reemplazan findAllActivos() + subList en memoria
// por una consulta SQL con LIMIT/OFFSET real.
public class PacienteRepositoryAdapterTest {

    @Test
    public void buscarPagina_delegaEnFindAllActivosConPageable() {
        PacienteJpaRepository jpaRepository = Mockito.mock(PacienteJpaRepository.class);
        PacienteEntity entity = new PacienteEntity(1L, "Juan Gómez", "30111222", null, null, null, null, null, null);
        when(jpaRepository.findAllActivos(eq(PageRequest.of(1, 10))))
                .thenReturn(new PageImpl<>(List.of(entity)));

        PacienteRepositoryAdapter adapter = new PacienteRepositoryAdapter(jpaRepository);

        List<Paciente> resultado = adapter.buscarPagina(1, 10);

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
    }

    @Test
    public void contar_delegaEnCountActivos() {
        PacienteJpaRepository jpaRepository = Mockito.mock(PacienteJpaRepository.class);
        when(jpaRepository.countActivos()).thenReturn(9L);

        PacienteRepositoryAdapter adapter = new PacienteRepositoryAdapter(jpaRepository);

        assertEquals(9L, adapter.contar());
    }
}
