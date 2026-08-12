package com.medconnect.application.usecase;

import com.medconnect.domain.model.Turno;
import com.medconnect.domain.port.TurnoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class CrearTurnoServiceTest {

    @Test
    public void crearTurno_guardaYDevuelveId() {
        TurnoRepository repo = Mockito.mock(TurnoRepository.class);

        when(repo.guardar(any(Turno.class))).thenAnswer(invocation -> {
            Turno t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        CrearTurnoService service = new CrearTurnoService(repo);

        CreateTurnoRequest req = new CreateTurnoRequest(
                LocalDateTime.of(2026, 8, 12, 10, 0),
                "Cardiología",
                2L,
                3L
        );

        CreateTurnoResponse resp = service.crear(req);

        assertEquals(1L, resp.getId());
    }
}
