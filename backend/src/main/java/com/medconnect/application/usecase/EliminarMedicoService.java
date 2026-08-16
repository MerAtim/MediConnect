package com.medconnect.application.usecase;

import com.medconnect.domain.port.MedicoRepository;
import org.springframework.stereotype.Service;

@Service
public class EliminarMedicoService implements EliminarMedicoUseCase {

    private final MedicoRepository medicoRepository;

    public EliminarMedicoService(MedicoRepository medicoRepository) {
        this.medicoRepository = medicoRepository;
    }

    @Override
    public boolean eliminar(Long id) {
        if (medicoRepository.buscarPorId(id).isEmpty()) {
            return false;
        }
        medicoRepository.eliminar(id);
        return true;
    }
}
