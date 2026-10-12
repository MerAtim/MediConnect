package com.medconnect.application.usecase;

import com.medconnect.domain.exception.EspecialidadDuracionInvalidaException;
import com.medconnect.domain.model.Medico;
import com.medconnect.domain.port.EspecialidadDuracionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GestionarDuracionEspecialidadServiceTest {

    @Test
    public void listarTodas_combinaConfiguradasConEspecialidadesDeMedicosSinFilaPropia() {
        EspecialidadDuracionRepository duracionRepository = Mockito.mock(EspecialidadDuracionRepository.class);
        BuscarMedicoUseCase buscarMedicoUseCase = Mockito.mock(BuscarMedicoUseCase.class);
        when(duracionRepository.buscarTodas()).thenReturn(Map.of("Cardiología", 45));
        when(buscarMedicoUseCase.buscarTodos()).thenReturn(List.of(
                new Medico(1L, "Dr A", "Cardiología", "M1", null, null, null),
                new Medico(2L, "Dr B", "Pediatría", "M2", null, null, null)));

        GestionarDuracionEspecialidadService service =
                new GestionarDuracionEspecialidadService(duracionRepository, buscarMedicoUseCase);

        List<EspecialidadDuracion> resultado = service.listarTodas();

        assertEquals(2, resultado.size());
        EspecialidadDuracion cardiologia = resultado.stream().filter(e -> e.getEspecialidad().equals("Cardiología")).findFirst().orElseThrow();
        EspecialidadDuracion pediatria = resultado.stream().filter(e -> e.getEspecialidad().equals("Pediatría")).findFirst().orElseThrow();
        assertEquals(45, cardiologia.getDuracionMinutos());
        assertEquals(EspecialidadDuracionRepository.DURACION_DEFAULT_MINUTOS, pediatria.getDuracionMinutos());
    }

    @Test
    public void listarTodas_ignoraEspecialidadNullOBlancaDeUnMedico() {
        EspecialidadDuracionRepository duracionRepository = Mockito.mock(EspecialidadDuracionRepository.class);
        BuscarMedicoUseCase buscarMedicoUseCase = Mockito.mock(BuscarMedicoUseCase.class);
        when(duracionRepository.buscarTodas()).thenReturn(Map.of());
        when(buscarMedicoUseCase.buscarTodos()).thenReturn(List.of(
                new Medico(1L, "Dr A", null, "M1", null, null, null),
                new Medico(2L, "Dr B", "  ", "M2", null, null, null)));

        GestionarDuracionEspecialidadService service =
                new GestionarDuracionEspecialidadService(duracionRepository, buscarMedicoUseCase);

        assertEquals(List.of(), service.listarTodas());
    }

    @Test
    public void establecer_delegaEnElRepositorio_siLosDatosSonValidos() {
        EspecialidadDuracionRepository duracionRepository = Mockito.mock(EspecialidadDuracionRepository.class);
        BuscarMedicoUseCase buscarMedicoUseCase = Mockito.mock(BuscarMedicoUseCase.class);
        GestionarDuracionEspecialidadService service =
                new GestionarDuracionEspecialidadService(duracionRepository, buscarMedicoUseCase);

        service.establecer("Cardiología", 45);

        verify(duracionRepository).establecer("Cardiología", 45);
    }

    @Test
    public void establecer_lanzaExcepcion_siEspecialidadEsBlanca() {
        EspecialidadDuracionRepository duracionRepository = Mockito.mock(EspecialidadDuracionRepository.class);
        BuscarMedicoUseCase buscarMedicoUseCase = Mockito.mock(BuscarMedicoUseCase.class);
        GestionarDuracionEspecialidadService service =
                new GestionarDuracionEspecialidadService(duracionRepository, buscarMedicoUseCase);

        assertThrows(EspecialidadDuracionInvalidaException.class, () -> service.establecer("  ", 45));
    }

    @Test
    public void establecer_lanzaExcepcion_siMinutosEsCeroONegativo() {
        EspecialidadDuracionRepository duracionRepository = Mockito.mock(EspecialidadDuracionRepository.class);
        BuscarMedicoUseCase buscarMedicoUseCase = Mockito.mock(BuscarMedicoUseCase.class);
        GestionarDuracionEspecialidadService service =
                new GestionarDuracionEspecialidadService(duracionRepository, buscarMedicoUseCase);

        assertThrows(EspecialidadDuracionInvalidaException.class, () -> service.establecer("Cardiología", 0));
        assertThrows(EspecialidadDuracionInvalidaException.class, () -> service.establecer("Cardiología", -5));
    }
}
