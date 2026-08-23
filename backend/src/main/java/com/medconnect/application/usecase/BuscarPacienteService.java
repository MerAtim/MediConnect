package com.medconnect.application.usecase;

import com.medconnect.domain.model.Paciente;
import com.medconnect.domain.port.PacienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BuscarPacienteService implements BuscarPacienteUseCase {

    private final PacienteRepository pacienteRepository;

    public BuscarPacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    @Override
    public Optional<Paciente> buscarPorId(Long id) {
        return pacienteRepository.buscarPorId(id);
    }

    @Override
    public Optional<Paciente> buscarPorEmail(String email) {
        return pacienteRepository.buscarPorEmail(email);
    }

    @Override
    public List<Paciente> buscarTodos() {
        return pacienteRepository.buscarTodos();
    }
}
