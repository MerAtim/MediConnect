package com.medconnect.infrastructure.persistence;

import com.medconnect.domain.model.Medico;
import com.medconnect.domain.model.Paciente;
import com.medconnect.domain.model.RegistroClinico;
import com.medconnect.domain.port.RegistroClinicoRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile("!test")
public class RegistroClinicoRepositoryAdapter implements RegistroClinicoRepository {

    private final RegistroClinicoJpaRepository jpaRepository;

    public RegistroClinicoRepositoryAdapter(RegistroClinicoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public RegistroClinico guardar(RegistroClinico registro) {
        RegistroClinicoEntity entity = new RegistroClinicoEntity(
                registro.getId(),
                registro.getFecha(),
                registro.getMedico() != null ? registro.getMedico().getId() : null,
                registro.getPaciente() != null ? registro.getPaciente().getId() : null,
                registro.getDiagnostico(),
                registro.getTratamiento(),
                registro.getObservaciones()
        );
        RegistroClinicoEntity guardado = jpaRepository.save(entity);
        registro.setId(guardado.getId());
        return registro;
    }

    @Override
    public List<RegistroClinico> buscarPorPaciente(Long pacienteId) {
        return jpaRepository.findByPacienteIdOrderByFechaDesc(pacienteId).stream().map(this::toDomain).toList();
    }

    private RegistroClinico toDomain(RegistroClinicoEntity entity) {
        Medico medico = entity.getMedicoId() != null
                ? new Medico(entity.getMedicoId(), null, null, null, null, null, null, null)
                : null;
        Paciente paciente = entity.getPacienteId() != null
                ? new Paciente(entity.getPacienteId(), null, null, null, null, null, null, null, null)
                : null;
        return new RegistroClinico(entity.getId(), entity.getFecha(), medico, paciente,
                entity.getDiagnostico(), entity.getTratamiento(), entity.getObservaciones());
    }
}
