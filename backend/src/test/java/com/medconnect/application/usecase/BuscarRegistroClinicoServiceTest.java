package com.medconnect.application.usecase;

import com.medconnect.domain.model.RegistroClinico;
import com.medconnect.domain.port.RegistroClinicoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class BuscarRegistroClinicoServiceTest {

    @Test
    public void buscarPorPaciente_delegaAlRepositorio() {
        RegistroClinicoRepository repo = Mockito.mock(RegistroClinicoRepository.class);
        RegistroClinico registro = new RegistroClinico(1L, null, null, null, "Fractura", "Reposo", null);
        when(repo.buscarPorPaciente(3L)).thenReturn(List.of(registro));

        BuscarRegistroClinicoService service = new BuscarRegistroClinicoService(repo);

        List<RegistroClinico> resultado = service.buscarPorPaciente(3L);

        assertEquals(1, resultado.size());
        assertEquals("Fractura", resultado.get(0).getDiagnostico());
    }
}
