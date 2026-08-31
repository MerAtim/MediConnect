package com.medconnect.application.usecase;

import com.medconnect.domain.model.Medico;
import com.medconnect.domain.port.MedicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BuscarMedicoService implements BuscarMedicoUseCase {

    private final MedicoRepository medicoRepository;

    public BuscarMedicoService(MedicoRepository medicoRepository) {
        this.medicoRepository = medicoRepository;
    }

    @Override
    public Optional<Medico> buscarPorId(Long id) {
        return medicoRepository.buscarPorId(id);
    }

    @Override
    public Map<Long, Medico> buscarPorIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return medicoRepository.buscarPorIds(ids).stream()
                .collect(Collectors.toMap(Medico::getId, Function.identity()));
    }

    @Override
    public Optional<Medico> buscarPorEmail(String email) {
        return medicoRepository.buscarPorEmail(email);
    }

    @Override
    public List<Medico> buscarTodos() {
        return medicoRepository.buscarTodos();
    }
}
