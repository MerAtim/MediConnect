package com.medconnect.application.usecase;

import com.medconnect.domain.exception.PacienteInvalidoException;
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
        if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
            throw new PacienteInvalidoException("nombre es obligatorio");
        }
        if (request.getDni() == null || request.getDni().trim().isEmpty()) {
            throw new PacienteInvalidoException("dni es obligatorio");
        }

        Paciente paciente = new Paciente(
                null,
                request.getNombre(),
                request.getDni(),
                request.getTelefono(),
                request.getDireccion(),
                request.getObraSocial(),
                request.getEmail()
        );

        Paciente guardado = pacienteRepository.guardar(paciente);
        return new CreatePacienteResponse(guardado.getId());
    }
}
