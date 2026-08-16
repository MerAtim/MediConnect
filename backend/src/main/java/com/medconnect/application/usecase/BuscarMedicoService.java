package com.medconnect.application.usecase;

import com.medconnect.domain.model.Medico;
import com.medconnect.domain.port.MedicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
    public List<Medico> buscarTodos() {
        return medicoRepository.buscarTodos();
    }
}
