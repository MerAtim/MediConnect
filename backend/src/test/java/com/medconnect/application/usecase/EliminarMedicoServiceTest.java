package com.medconnect.application.usecase;

import com.medconnect.domain.model.Medico;
import com.medconnect.domain.port.MedicoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class EliminarMedicoServiceTest {

    @Test
    public void eliminar_devuelveTrueYBorra_siExiste() {
        MedicoRepository repo = Mockito.mock(MedicoRepository.class);
        when(repo.buscarPorId(1L)).thenReturn(Optional.of(new Medico(1L, "Ana Pérez", "Cardiología", "MP1234", null, null, null, null)));

        EliminarMedicoService service = new EliminarMedicoService(repo);

        assertTrue(service.eliminar(1L));
        Mockito.verify(repo).eliminar(1L);
    }

    @Test
    public void eliminar_devuelveFalse_siNoExiste() {
        MedicoRepository repo = Mockito.mock(MedicoRepository.class);
        when(repo.buscarPorId(99L)).thenReturn(Optional.empty());

        EliminarMedicoService service = new EliminarMedicoService(repo);

        assertFalse(service.eliminar(99L));
        Mockito.verify(repo, Mockito.never()).eliminar(Mockito.anyLong());
    }
}
