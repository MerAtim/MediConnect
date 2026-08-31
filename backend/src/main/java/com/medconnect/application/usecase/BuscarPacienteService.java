package com.medconnect.application.usecase;

import com.medconnect.domain.model.Paciente;
import com.medconnect.domain.port.PacienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public Map<Long, Paciente> buscarPorIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return pacienteRepository.buscarPorIds(ids).stream()
                .collect(Collectors.toMap(Paciente::getId, Function.identity()));
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
