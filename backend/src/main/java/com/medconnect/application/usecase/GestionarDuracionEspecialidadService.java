package com.medconnect.application.usecase;

import com.medconnect.domain.exception.EspecialidadDuracionInvalidaException;
import com.medconnect.domain.model.Medico;
import com.medconnect.domain.port.EspecialidadDuracionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

@Service
public class GestionarDuracionEspecialidadService implements GestionarDuracionEspecialidadUseCase {

    private final EspecialidadDuracionRepository especialidadDuracionRepository;
    private final BuscarMedicoUseCase buscarMedicoUseCase;

    public GestionarDuracionEspecialidadService(EspecialidadDuracionRepository especialidadDuracionRepository,
                                                 BuscarMedicoUseCase buscarMedicoUseCase) {
        this.especialidadDuracionRepository = especialidadDuracionRepository;
        this.buscarMedicoUseCase = buscarMedicoUseCase;
    }

    @Override
    public List<EspecialidadDuracion> listarTodas() {
        Map<String, Integer> configuradas = especialidadDuracionRepository.buscarTodas();

        TreeSet<String> especialidades = new TreeSet<>(configuradas.keySet());
        buscarMedicoUseCase.buscarTodos().stream()
                .map(Medico::getEspecialidad)
                .filter(e -> e != null && !e.isBlank())
                .forEach(especialidades::add);

        return especialidades.stream()
                .map(e -> new EspecialidadDuracion(e,
                        configuradas.getOrDefault(e, EspecialidadDuracionRepository.DURACION_DEFAULT_MINUTOS)))
                .toList();
    }

    @Override
    public void establecer(String especialidad, int minutos) {
        if (especialidad == null || especialidad.isBlank()) {
            throw new EspecialidadDuracionInvalidaException("especialidad es obligatoria");
        }
        if (minutos <= 0) {
            throw new EspecialidadDuracionInvalidaException("duracionMinutos debe ser mayor a cero");
        }
        especialidadDuracionRepository.establecer(especialidad, minutos);
    }
}
