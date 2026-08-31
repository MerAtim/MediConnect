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
        request.validar();

        String email = ValidacionEmail.normalizar(request.getEmail());
        ValidacionEmail.asegurarDisponible(email, pacienteRepository::buscarPorEmail, Paciente::getId, id,
                () -> new PacienteInvalidoException("ya existe un paciente con ese email"));

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
}
