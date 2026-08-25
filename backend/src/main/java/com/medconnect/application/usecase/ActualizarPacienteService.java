package com.medconnect.application.usecase;

import com.medconnect.domain.exception.PacienteInvalidoException;
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
        if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
            throw new PacienteInvalidoException("nombre es obligatorio");
        }
        if (request.getDni() == null || request.getDni().trim().isEmpty()) {
            throw new PacienteInvalidoException("dni es obligatorio");
        }

        String email = normalizarEmail(request.getEmail());
        if (email != null) {
            Optional<Paciente> existente = pacienteRepository.buscarPorEmail(email);
            if (existente.isPresent() && !existente.get().getId().equals(id)) {
                throw new PacienteInvalidoException("ya existe un paciente con ese email");
            }
        }

        Paciente paciente = new Paciente(
                id,
                request.getNombre(),
                request.getDni(),
                request.getTelefono(),
                request.getDireccion(),
                request.getObraSocial(),
                request.getNumeroAfiliado(),
                request.getPlan(),
                email
        );

        return Optional.of(pacienteRepository.guardar(paciente));
    }

    private String normalizarEmail(String email) {
        return (email == null || email.trim().isEmpty()) ? null : email.trim();
    }
}
