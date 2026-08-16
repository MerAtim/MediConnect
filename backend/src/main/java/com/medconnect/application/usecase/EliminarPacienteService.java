package com.medconnect.application.usecase;

import com.medconnect.domain.port.PacienteRepository;
import org.springframework.stereotype.Service;

@Service
public class EliminarPacienteService implements EliminarPacienteUseCase {

    private final PacienteRepository pacienteRepository;

    public EliminarPacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    @Override
    public boolean eliminar(Long id) {
        if (pacienteRepository.buscarPorId(id).isEmpty()) {
            return false;
        }
        pacienteRepository.eliminar(id);
        return true;
    }
}
