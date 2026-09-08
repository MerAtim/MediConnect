package com.medconnect.application.usecase;

import com.medconnect.domain.model.Medico;
import com.medconnect.domain.port.MedicoRepository;
import org.springframework.stereotype.Service;

@Service
public class CrearMedicoService implements CrearMedicoUseCase {

    private final MedicoRepository medicoRepository;

    public CrearMedicoService(MedicoRepository medicoRepository) {
        this.medicoRepository = medicoRepository;
    }

    @Override
    public CreateMedicoResponse crear(CreateMedicoRequest request) {
        Medico medico = MedicoFactory.crear(null, request, medicoRepository::buscarPorEmail,
                medicoRepository::existeEmailEnPerfilEliminado);
        Medico guardado = medicoRepository.guardar(medico);
        return new CreateMedicoResponse(guardado.getId());
    }
}
