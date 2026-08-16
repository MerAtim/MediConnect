package com.medconnect.infrastructure.persistence;

import com.medconnect.domain.model.Paciente;
import com.medconnect.domain.port.PacienteRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile("!test")
public class PacienteRepositoryAdapter implements PacienteRepository {

    private final PacienteJpaRepository jpaRepository;

    public PacienteRepositoryAdapter(PacienteJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Paciente guardar(Paciente paciente) {
        PacienteEntity entity = new PacienteEntity(
                paciente.getId(),
                paciente.getNombre(),
                paciente.getDni(),
                paciente.getTelefono(),
                paciente.getDireccion(),
                paciente.getObraSocial(),
                paciente.getNumeroAfiliado(),
                paciente.getPlan(),
                paciente.getEmail()
        );
        PacienteEntity guardado = jpaRepository.save(entity);
        paciente.setId(guardado.getId());
        return paciente;
    }

    @Override
    public Optional<Paciente> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Paciente> buscarTodos() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    private Paciente toDomain(PacienteEntity entity) {
        return new Paciente(
                entity.getId(),
                entity.getNombre(),
                entity.getDni(),
                entity.getTelefono(),
                entity.getDireccion(),
                entity.getObraSocial(),
                entity.getNumeroAfiliado(),
                entity.getPlan(),
                entity.getEmail()
        );
    }
}
