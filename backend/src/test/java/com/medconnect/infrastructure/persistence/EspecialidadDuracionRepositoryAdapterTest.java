package com.medconnect.infrastructure.persistence;

import com.medconnect.domain.port.EspecialidadDuracionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EspecialidadDuracionRepositoryAdapterTest {

    @Test
    public void buscarTodas_mapeaCadaFilaAEspecialidadYDuracion() {
        EspecialidadDuracionJpaRepository jpaRepository = Mockito.mock(EspecialidadDuracionJpaRepository.class);
        when(jpaRepository.findAll()).thenReturn(List.of(
                new EspecialidadDuracionEntity("Cardiología", 45),
                new EspecialidadDuracionEntity("Pediatría", 20)));
        EspecialidadDuracionRepositoryAdapter adapter = new EspecialidadDuracionRepositoryAdapter(jpaRepository);

        Map<String, Integer> resultado = adapter.buscarTodas();

        assertEquals(Map.of("Cardiología", 45, "Pediatría", 20), resultado);
    }

    @Test
    public void buscarDuracion_devuelveLaConfigurada_siExisteFila() {
        EspecialidadDuracionJpaRepository jpaRepository = Mockito.mock(EspecialidadDuracionJpaRepository.class);
        when(jpaRepository.findById("Cardiología")).thenReturn(Optional.of(new EspecialidadDuracionEntity("Cardiología", 45)));
        EspecialidadDuracionRepositoryAdapter adapter = new EspecialidadDuracionRepositoryAdapter(jpaRepository);

        assertEquals(45, adapter.buscarDuracion("Cardiología"));
    }

    @Test
    public void buscarDuracion_devuelveElDefault_siNoHayFila() {
        EspecialidadDuracionJpaRepository jpaRepository = Mockito.mock(EspecialidadDuracionJpaRepository.class);
        when(jpaRepository.findById("Dermatología")).thenReturn(Optional.empty());
        EspecialidadDuracionRepositoryAdapter adapter = new EspecialidadDuracionRepositoryAdapter(jpaRepository);

        assertEquals(EspecialidadDuracionRepository.DURACION_DEFAULT_MINUTOS, adapter.buscarDuracion("Dermatología"));
    }

    @Test
    public void establecer_guardaLaEntidadConLaEspecialidadYLosMinutosDados() {
        EspecialidadDuracionJpaRepository jpaRepository = Mockito.mock(EspecialidadDuracionJpaRepository.class);
        EspecialidadDuracionRepositoryAdapter adapter = new EspecialidadDuracionRepositoryAdapter(jpaRepository);

        adapter.establecer("Cardiología", 45);

        verify(jpaRepository).save(Mockito.argThat(entity ->
                "Cardiología".equals(entity.getEspecialidad()) && Integer.valueOf(45).equals(entity.getDuracionMinutos())));
    }
}
