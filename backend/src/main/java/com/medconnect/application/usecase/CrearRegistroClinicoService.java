package com.medconnect.application.usecase;

import com.medconnect.domain.exception.RegistroClinicoInvalidoException;
import com.medconnect.domain.model.Medico;
import com.medconnect.domain.model.Paciente;
import com.medconnect.domain.model.RegistroClinico;
import com.medconnect.domain.port.MedicoRepository;
import com.medconnect.domain.port.PacienteRepository;
import com.medconnect.domain.port.RegistroClinicoRepository;
import com.medconnect.domain.port.TurnoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CrearRegistroClinicoService implements CrearRegistroClinicoUseCase {

    private final RegistroClinicoRepository registroClinicoRepository;
    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;
    private final TurnoRepository turnoRepository;

    public CrearRegistroClinicoService(RegistroClinicoRepository registroClinicoRepository, MedicoRepository medicoRepository,
                                        PacienteRepository pacienteRepository, TurnoRepository turnoRepository) {
        this.registroClinicoRepository = registroClinicoRepository;
        this.medicoRepository = medicoRepository;
        this.pacienteRepository = pacienteRepository;
        this.turnoRepository = turnoRepository;
    }

    @Override
    public CreateRegistroClinicoResponse crear(CreateRegistroClinicoRequest request) {
        if (request.getMedicoId() == null) {
            throw new RegistroClinicoInvalidoException("medicoId es obligatorio");
        }
        if (request.getPacienteId() == null) {
            throw new RegistroClinicoInvalidoException("pacienteId es obligatorio");
        }
        if (request.getDiagnostico() == null || request.getDiagnostico().trim().isEmpty()) {
            throw new RegistroClinicoInvalidoException("diagnostico es obligatorio");
        }
        if (request.getTratamiento() == null || request.getTratamiento().trim().isEmpty()) {
            throw new RegistroClinicoInvalidoException("tratamiento es obligatorio");
        }
        if (medicoRepository.buscarPorId(request.getMedicoId()).isEmpty()) {
            throw new RegistroClinicoInvalidoException("El médico indicado no existe");
        }
        if (pacienteRepository.buscarPorId(request.getPacienteId()).isEmpty()) {
            throw new RegistroClinicoInvalidoException("El paciente indicado no existe");
        }

        boolean tieneTurno = turnoRepository.buscarPorMedico(request.getMedicoId()).stream()
                .anyMatch(t -> t.getPaciente() != null && request.getPacienteId().equals(t.getPaciente().getId()));
        if (!tieneTurno) {
            throw new RegistroClinicoInvalidoException("El médico no tiene ningún turno con ese paciente");
        }

        Medico medico = new Medico(request.getMedicoId(), null, null, null, null, null, null, null);
        Paciente paciente = new Paciente(request.getPacienteId(), null, null, null, null, null, null, null, null);

        RegistroClinico registro = new RegistroClinico(
                null,
                LocalDateTime.now(),
                medico,
                paciente,
                request.getDiagnostico(),
                request.getTratamiento(),
                request.getObservaciones()
        );

        RegistroClinico guardado = registroClinicoRepository.guardar(registro);
        return new CreateRegistroClinicoResponse(guardado.getId());
    }
}
