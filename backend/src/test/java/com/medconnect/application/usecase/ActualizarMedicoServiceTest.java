package com.medconnect.application.usecase;

import com.medconnect.domain.exception.MedicoInvalidoException;
import com.medconnect.domain.model.Medico;
import com.medconnect.domain.port.MedicoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ActualizarMedicoServiceTest {

    @Test
    public void actualizar_devuelveVacio_siNoExiste() {
        MedicoRepository repo = Mockito.mock(MedicoRepository.class);
        when(repo.buscarPorId(99L)).thenReturn(Optional.empty());

        ActualizarMedicoService service = new ActualizarMedicoService(repo);

        CreateMedicoRequest req = new CreateMedicoRequest("Ana Pérez", "Cardiología", "MP1234", null, null, null);

        assertTrue(service.actualizar(99L, req).isEmpty());
    }

    @Test
    public void actualizar_lanzaExcepcion_siFaltaNombre() {
        MedicoRepository repo = Mockito.mock(MedicoRepository.class);
        when(repo.buscarPorId(1L)).thenReturn(Optional.of(new Medico(1L, "Ana Pérez", "Cardiología", "MP1234", null, null, null, null)));

        ActualizarMedicoService service = new ActualizarMedicoService(repo);

        CreateMedicoRequest req = new CreateMedicoRequest("", "Cardiología", "MP1234", null, null, null);

        assertThrows(MedicoInvalidoException.class, () -> service.actualizar(1L, req));
    }

    @Test
    public void actualizar_guardaYDevuelveMedicoActualizado() {
        MedicoRepository repo = Mockito.mock(MedicoRepository.class);
        when(repo.buscarPorId(1L)).thenReturn(Optional.of(new Medico(1L, "Ana Pérez", "Cardiología", "MP1234", null, null, null, null)));
        when(repo.guardar(any(Medico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ActualizarMedicoService service = new ActualizarMedicoService(repo);

        CreateMedicoRequest req = new CreateMedicoRequest("Ana Pérez", "Clínica Médica", "MP1234", null, null, null);

        Optional<Medico> resultado = service.actualizar(1L, req);

        assertTrue(resultado.isPresent());
        assertEquals("Clínica Médica", resultado.get().getEspecialidad());
    }
}
