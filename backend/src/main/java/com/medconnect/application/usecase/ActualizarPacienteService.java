package com.medconnect.application.usecase;

import com.medconnect.domain.model.Paciente;
import com.medconnect.domain.port.PacienteRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ActualizarPacienteService implements ActualizarPacienteUseCase {

    private final PacienteRepository pacienteRepository;

    public ActualizarPacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    @Override
    public Optional<Paciente> actualizar(Long id, CreatePacienteRequest request) {
        if (pacienteRepository.buscarPorId(id).isEmpty()) {
            return Optional.empty();
        }

        Paciente paciente = PacienteFactory.crear(id, request, pacienteRepository::buscarPorEmail);
        return Optional.of(pacienteRepository.guardar(paciente));
    }
}
