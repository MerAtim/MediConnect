package com.medconnect.infrastructure.persistence;

import com.medconnect.domain.model.Medico;
import com.medconnect.domain.port.MedicoRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile("!test")
public class MedicoRepositoryAdapter implements MedicoRepository {

    private final MedicoJpaRepository jpaRepository;

    public MedicoRepositoryAdapter(MedicoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Medico guardar(Medico medico) {
        MedicoEntity entity = new MedicoEntity(
                medico.getId(),
                medico.getNombre(),
                medico.getEspecialidad(),
                medico.getMatricula(),
                medico.getDireccion(),
                medico.getTelefono(),
                medico.getEmail()
        );
        MedicoEntity guardado = jpaRepository.save(entity);
        medico.setId(guardado.getId());
        return medico;
    }

    @Override
    public Optional<Medico> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Medico> buscarTodos() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    private Medico toDomain(MedicoEntity entity) {
        return new Medico(
                entity.getId(),
                entity.getNombre(),
                entity.getEspecialidad(),
                entity.getMatricula(),
                entity.getDireccion(),
                entity.getTelefono(),
                entity.getEmail(),
                null
        );
    }
}
