package com.medconnect.application.usecase;

import com.medconnect.domain.exception.PacienteInvalidoException;
import com.medconnect.domain.model.Paciente;
import com.medconnect.domain.port.PacienteRepository;
import org.springframework.stereotype.Service;

@Service
public class CrearPacienteService implements CrearPacienteUseCase {

    private final PacienteRepository pacienteRepository;

    public CrearPacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    @Override
    public CreatePacienteResponse crear(CreatePacienteRequest request) {
        request.validar();

        String email = ValidacionEmail.normalizar(request.getEmail());
        ValidacionEmail.asegurarDisponible(email, pacienteRepository::buscarPorEmail, Paciente::getId, null,
                () -> new PacienteInvalidoException("ya existe un paciente con ese email"));

        Paciente paciente = new Paciente(
                null,
                request.getNombre(),
                request.getDni(),
                request.getTelefono(),
                request.getDireccion(),
                request.getObraSocial(),
                request.getNumeroAfiliado(),
                request.getPlan(),
                email
        );

        Paciente guardado = pacienteRepository.guardar(paciente);
        return new CreatePacienteResponse(guardado.getId());
    }
}
