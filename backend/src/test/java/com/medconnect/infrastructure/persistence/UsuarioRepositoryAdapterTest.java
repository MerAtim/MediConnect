package com.medconnect.infrastructure.persistence;

import com.medconnect.domain.model.Usuario;
import com.medconnect.domain.model.UsuarioRole;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

// LOW de la re-auditoria e2e (2026-09-08, segunda ronda): "paginacion falsa"
// -- buscarPagina/contar reemplazan findAll() + subList en memoria por
// findAll(Pageable)/count(), ya heredados de JpaRepository (sin necesidad de
// @Query propia: usuarios no tiene soft-delete).
public class UsuarioRepositoryAdapterTest {

    @Test
    public void buscarPagina_delegaEnFindAllConPageable() {
        UsuarioJpaRepository jpaRepository = Mockito.mock(UsuarioJpaRepository.class);
        UsuarioEntity entity = new UsuarioEntity(1L, "Ana Pérez", "ana@medconnect.com", "hash", UsuarioRole.MEDICO);
        when(jpaRepository.findAll(eq(PageRequest.of(0, 20))))
                .thenReturn(new PageImpl<>(List.of(entity)));

        UsuarioRepositoryAdapter adapter = new UsuarioRepositoryAdapter(jpaRepository);

        List<Usuario> resultado = adapter.buscarPagina(0, 20);

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
    }

    @Test
    public void contar_delegaEnCount() {
        UsuarioJpaRepository jpaRepository = Mockito.mock(UsuarioJpaRepository.class);
        when(jpaRepository.count()).thenReturn(3L);

        UsuarioRepositoryAdapter adapter = new UsuarioRepositoryAdapter(jpaRepository);

        assertEquals(3L, adapter.contar());
    }
}
