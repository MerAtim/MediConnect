package com.medconnect.application.usecase;

import com.medconnect.domain.exception.MedicoInvalidoException;
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
        if (email != null) {
            Optional<Medico> existente = medicoRepository.buscarPorEmail(email);
            if (existente.isPresent() && !existente.get().getId().equals(id)) {
                throw new MedicoInvalidoException("ya existe un medico con ese email");
            }
        }

        Medico medico = new Medico(
                id,
                request.getNombre(),
                request.getEspecialidad(),
                request.getMatricula(),
                request.getDireccion(),
                request.getTelefono(),
                email,
                null
        );

        return Optional.of(medicoRepository.guardar(medico));
    }

    private String normalizarEmail(String email) {
        return (email == null || email.trim().isEmpty()) ? null : email.trim();
    }
}
