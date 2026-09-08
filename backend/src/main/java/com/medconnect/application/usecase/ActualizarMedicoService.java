package com.medconnect.application.usecase;

import com.medconnect.domain.model.Medico;
import com.medconnect.domain.port.MedicoRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ActualizarMedicoService implements ActualizarMedicoUseCase {

    private final MedicoRepository medicoRepository;

    public ActualizarMedicoService(MedicoRepository medicoRepository) {
        this.medicoRepository = medicoRepository;
    }

    @Override
    public Optional<Medico> actualizar(Long id, CreateMedicoRequest request) {
        if (medicoRepository.buscarPorId(id).isEmpty()) {
            return Optional.empty();
        }

        Medico medico = MedicoFactory.crear(id, request, medicoRepository::buscarPorEmail);
        return Optional.of(medicoRepository.guardar(medico));
    }
}
