package com.medconnect.application.usecase;

import com.medconnect.domain.model.RegistroClinico;
import com.medconnect.domain.port.RegistroClinicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuscarRegistroClinicoService implements BuscarRegistroClinicoUseCase {

    private final RegistroClinicoRepository registroClinicoRepository;

    public BuscarRegistroClinicoService(RegistroClinicoRepository registroClinicoRepository) {
        this.registroClinicoRepository = registroClinicoRepository;
    }

    @Override
    public List<RegistroClinico> buscarPorPaciente(Long pacienteId) {
        return registroClinicoRepository.buscarPorPaciente(pacienteId);
    }
}
