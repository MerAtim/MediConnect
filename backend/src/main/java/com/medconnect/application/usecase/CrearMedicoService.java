package com.medconnect.application.usecase;

import com.medconnect.domain.exception.MedicoInvalidoException;
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
        if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
            throw new MedicoInvalidoException("nombre es obligatorio");
        }
        if (request.getEspecialidad() == null || request.getEspecialidad().trim().isEmpty()) {
            throw new MedicoInvalidoException("especialidad es obligatoria");
        }
        if (request.getMatricula() == null || request.getMatricula().trim().isEmpty()) {
            throw new MedicoInvalidoException("matricula es obligatoria");
        }

        String email = normalizarEmail(request.getEmail());
        if (email != null && medicoRepository.buscarPorEmail(email).isPresent()) {
            throw new MedicoInvalidoException("ya existe un medico con ese email");
        }

        Medico medico = new Medico(
                null,
                request.getNombre(),
                request.getEspecialidad(),
                request.getMatricula(),
                request.getDireccion(),
                request.getTelefono(),
                email,
                null
        );

        Medico guardado = medicoRepository.guardar(medico);
        return new CreateMedicoResponse(guardado.getId());
    }

    private String normalizarEmail(String email) {
        return (email == null || email.trim().isEmpty()) ? null : email.trim();
    }
}
