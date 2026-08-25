package com.medconnect.application.usecase;

import com.medconnect.domain.exception.PacienteInvalidoException;
import com.medconnect.domain.model.Paciente;
import com.medconnect.domain.port.PacienteRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ActualizarPacienteServiceTest {

    @Test
    public void actualizar_devuelveVacio_siNoExiste() {
        PacienteRepository repo = Mockito.mock(PacienteRepository.class);
        when(repo.buscarPorId(99L)).thenReturn(Optional.empty());

        ActualizarPacienteService service = new ActualizarPacienteService(repo);

        CreatePacienteRequest req = new CreatePacienteRequest("Juan Gómez", "30111222", null, null, null, null, null, null);

        assertTrue(service.actualizar(99L, req).isEmpty());
    }

    @Test
    public void actualizar_lanzaExcepcion_siFaltaDni() {
        PacienteRepository repo = Mockito.mock(PacienteRepository.class);
        when(repo.buscarPorId(1L)).thenReturn(Optional.of(new Paciente(1L, "Juan Gómez", "30111222", null, null, null, null, null, null)));

        ActualizarPacienteService service = new ActualizarPacienteService(repo);

        CreatePacienteRequest req = new CreatePacienteRequest("Juan Gómez", null, null, null, null, null, null, null);

        assertThrows(PacienteInvalidoException.class, () -> service.actualizar(1L, req));
    }

    @Test
    public void actualizar_guardaYDevuelvePacienteActualizado() {
        PacienteRepository repo = Mockito.mock(PacienteRepository.class);
        when(repo.buscarPorId(1L)).thenReturn(Optional.of(new Paciente(1L, "Juan Gómez", "30111222", null, null, null, null, null, null)));
        when(repo.guardar(any(Paciente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ActualizarPacienteService service = new ActualizarPacienteService(repo);

        CreatePacienteRequest req = new CreatePacienteRequest("Juan Gómez", "30111222", null, null, "Swiss Medical", "1122", "SMG20", null);

        Optional<Paciente> resultado = service.actualizar(1L, req);

        assertTrue(resultado.isPresent());
        assertEquals("SMG20", resultado.get().getPlan());
    }

    @Test
    public void actualizar_lanzaExcepcion_siEmailYaUsadoPorOtroPaciente() {
        PacienteRepository repo = Mockito.mock(PacienteRepository.class);
        when(repo.buscarPorId(1L)).thenReturn(Optional.of(new Paciente(1L, "Juan Gómez", "30111222", null, null, null, null, null, null)));
        when(repo.buscarPorEmail("otro@mail.com"))
                .thenReturn(Optional.of(new Paciente(2L, "Otro", "30999888", null, null, null, null, null, "otro@mail.com")));

        ActualizarPacienteService service = new ActualizarPacienteService(repo);

        CreatePacienteRequest req = new CreatePacienteRequest("Juan Gómez", "30111222", null, null, null, null, null, "otro@mail.com");

        assertThrows(PacienteInvalidoException.class, () -> service.actualizar(1L, req));
    }

    @Test
    public void actualizar_permiteConservarSuPropioEmail() {
        PacienteRepository repo = Mockito.mock(PacienteRepository.class);
        when(repo.buscarPorId(1L)).thenReturn(Optional.of(new Paciente(1L, "Juan Gómez", "30111222", null, null, null, null, null, "juan@mail.com")));
        when(repo.buscarPorEmail("juan@mail.com"))
                .thenReturn(Optional.of(new Paciente(1L, "Juan Gómez", "30111222", null, null, null, null, null, "juan@mail.com")));
        when(repo.guardar(any(Paciente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ActualizarPacienteService service = new ActualizarPacienteService(repo);

        CreatePacienteRequest req = new CreatePacienteRequest("Juan Gómez", "30111222", null, null, "Swiss Medical", "1122", "SMG20", "juan@mail.com");

        assertTrue(service.actualizar(1L, req).isPresent());
    }
}
