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
        request.validar();

        String email = ValidacionEmail.normalizar(request.getEmail());
        ValidacionEmail.asegurarDisponible(email, medicoRepository::buscarPorEmail, Medico::getId, null,
                () -> new MedicoInvalidoException("ya existe un medico con ese email"));

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
}
