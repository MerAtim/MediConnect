package com.medconnect.application.usecase;

import com.medconnect.domain.model.Usuario;
import com.medconnect.domain.model.UsuarioRole;
import com.medconnect.domain.port.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class BuscarUsuarioServiceTest {

    @Test
    public void buscarTodos_delegaAlRepositorio() {
        UsuarioRepository repo = Mockito.mock(UsuarioRepository.class);
        when(repo.buscarTodos()).thenReturn(List.of(
                new Usuario(1L, "Ana Pérez", "ana@medconnect.com", "hash", UsuarioRole.MEDICO)));

        BuscarUsuarioService service = new BuscarUsuarioService(repo);

        List<Usuario> resultado = service.buscarTodos();

        assertEquals(1, resultado.size());
        assertEquals("ana@medconnect.com", resultado.get(0).getEmail());
    }
}
