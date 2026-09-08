package com.medconnect.application.usecase;

import com.medconnect.domain.model.Paciente;
import com.medconnect.domain.port.PacienteRepository;
import org.springframework.stereotype.Service;

@Service
public class CrearPacienteService implements CrearPacienteUseCase {

    private final PacienteRepository pacienteRepository;

    public CrearPacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    @Override
    public CreatePacienteResponse crear(CreatePacienteRequest request) {
        Paciente paciente = PacienteFactory.crear(null, request, pacienteRepository::buscarPorEmail);
        Paciente guardado = pacienteRepository.guardar(paciente);
        return new CreatePacienteResponse(guardado.getId());
    }
}
