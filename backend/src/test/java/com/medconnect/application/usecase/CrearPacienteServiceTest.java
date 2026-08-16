package com.medconnect.application.usecase;

import com.medconnect.domain.exception.PacienteInvalidoException;
import com.medconnect.domain.model.Paciente;
import com.medconnect.domain.port.PacienteRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class CrearPacienteServiceTest {

    @Test
    public void crearPaciente_guardaYDevuelveId() {
        PacienteRepository repo = Mockito.mock(PacienteRepository.class);

        when(repo.guardar(any(Paciente.class))).thenAnswer(invocation -> {
            Paciente p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        CrearPacienteService service = new CrearPacienteService(repo);

        CreatePacienteRequest req = new CreatePacienteRequest(
                "Juan Gómez", "30111222", "1122334455", "Calle Falsa 123", "OSDE", "juan@mail.com"
        );

        CreatePacienteResponse resp = service.crear(req);

        assertEquals(1L, resp.getId());
    }

    @Test
    public void crearPaciente_lanzaExcepcion_siFaltaNombre() {
        PacienteRepository repo = Mockito.mock(PacienteRepository.class);
        CrearPacienteService service = new CrearPacienteService(repo);

        CreatePacienteRequest req = new CreatePacienteRequest(
                "", "30111222", null, null, null, null
        );

        assertThrows(PacienteInvalidoException.class, () -> service.crear(req));
    }

    @Test
    public void crearPaciente_lanzaExcepcion_siFaltaDni() {
        PacienteRepository repo = Mockito.mock(PacienteRepository.class);
        CrearPacienteService service = new CrearPacienteService(repo);

        CreatePacienteRequest req = new CreatePacienteRequest(
                "Juan Gómez", null, null, null, null, null
        );

        assertThrows(PacienteInvalidoException.class, () -> service.crear(req));
    }
}
