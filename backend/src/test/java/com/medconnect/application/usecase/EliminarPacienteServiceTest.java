package com.medconnect.application.usecase;

import com.medconnect.domain.model.Paciente;
import com.medconnect.domain.port.PacienteRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class EliminarPacienteServiceTest {

    @Test
    public void eliminar_devuelveTrueYBorra_siExiste() {
        PacienteRepository repo = Mockito.mock(PacienteRepository.class);
        when(repo.buscarPorId(1L)).thenReturn(Optional.of(new Paciente(1L, "Juan Gómez", "30111222", null, null, null, null, null, null)));

        EliminarPacienteService service = new EliminarPacienteService(repo);

        assertTrue(service.eliminar(1L));
        Mockito.verify(repo).eliminar(1L);
    }

    @Test
    public void eliminar_devuelveFalse_siNoExiste() {
        PacienteRepository repo = Mockito.mock(PacienteRepository.class);
        when(repo.buscarPorId(99L)).thenReturn(Optional.empty());

        EliminarPacienteService service = new EliminarPacienteService(repo);

        assertFalse(service.eliminar(99L));
        Mockito.verify(repo, Mockito.never()).eliminar(Mockito.anyLong());
    }
}
