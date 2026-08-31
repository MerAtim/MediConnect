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
        request.validar();

        String email = ValidacionEmail.normalizar(request.getEmail());
        ValidacionEmail.asegurarDisponible(email, medicoRepository::buscarPorEmail, Medico::getId, id,
                () -> new MedicoInvalidoException("ya existe un medico con ese email"));

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
}
