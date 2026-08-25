package com.medconnect.application.usecase;

import com.medconnect.domain.exception.MedicoInvalidoException;
import com.medconnect.domain.model.Medico;
import com.medconnect.domain.port.MedicoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class CrearMedicoServiceTest {

    @Test
    public void crearMedico_guardaYDevuelveId() {
        MedicoRepository repo = Mockito.mock(MedicoRepository.class);

        when(repo.guardar(any(Medico.class))).thenAnswer(invocation -> {
            Medico m = invocation.getArgument(0);
            m.setId(1L);
            return m;
        });

        CrearMedicoService service = new CrearMedicoService(repo);

        CreateMedicoRequest req = new CreateMedicoRequest(
                "Ana Pérez", "Cardiología", "MP1234", "Av. Siempreviva 742", "1122334455", "ana@medconnect.com"
        );

        CreateMedicoResponse resp = service.crear(req);

        assertEquals(1L, resp.getId());
    }

    @Test
    public void crearMedico_lanzaExcepcion_siFaltaNombre() {
        MedicoRepository repo = Mockito.mock(MedicoRepository.class);
        CrearMedicoService service = new CrearMedicoService(repo);

        CreateMedicoRequest req = new CreateMedicoRequest(
                "", "Cardiología", "MP1234", null, null, null
        );

        assertThrows(MedicoInvalidoException.class, () -> service.crear(req));
    }

    @Test
    public void crearMedico_lanzaExcepcion_siFaltaMatricula() {
        MedicoRepository repo = Mockito.mock(MedicoRepository.class);
        CrearMedicoService service = new CrearMedicoService(repo);

        CreateMedicoRequest req = new CreateMedicoRequest(
                "Ana Pérez", "Cardiología", null, null, null, null
        );

        assertThrows(MedicoInvalidoException.class, () -> service.crear(req));
    }

    @Test
    public void crearMedico_lanzaExcepcion_siEmailYaExiste() {
        MedicoRepository repo = Mockito.mock(MedicoRepository.class);
        when(repo.buscarPorEmail("ana@medconnect.com"))
                .thenReturn(Optional.of(new Medico(1L, "Otra", "Clínica Médica", "MP9999", null, null, "ana@medconnect.com", null)));

        CrearMedicoService service = new CrearMedicoService(repo);

        CreateMedicoRequest req = new CreateMedicoRequest(
                "Ana Pérez", "Cardiología", "MP1234", null, null, "ana@medconnect.com"
        );

        assertThrows(MedicoInvalidoException.class, () -> service.crear(req));
    }

    @Test
    public void crearMedico_permiteEmailVacio_sinChocarConOtrosSinVincular() {
        MedicoRepository repo = Mockito.mock(MedicoRepository.class);
        when(repo.guardar(any(Medico.class))).thenAnswer(invocation -> {
            Medico m = invocation.getArgument(0);
            m.setId(2L);
            return m;
        });

        CrearMedicoService service = new CrearMedicoService(repo);

        CreateMedicoRequest req = new CreateMedicoRequest(
                "Otro Médico", "Cardiología", "MP5555", null, null, ""
        );

        CreateMedicoResponse resp = service.crear(req);

        assertEquals(2L, resp.getId());
        Mockito.verify(repo, Mockito.never()).buscarPorEmail(any());
    }
}
