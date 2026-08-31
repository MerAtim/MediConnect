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
        return jpaRepository.findActivoById(id).map(this::toDomain);
    }

    @Override
    public List<Medico> buscarPorIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return jpaRepository.findActivosByIdIn(ids).stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Medico> buscarPorEmail(String email) {
        return jpaRepository.findActivoByEmail(email).map(this::toDomain);
    }

    @Override
    public List<Medico> buscarTodos() {
        return jpaRepository.findAllActivos().stream().map(this::toDomain).toList();
    }

    @Override
    public void eliminar(Long id) {
        jpaRepository.findById(id).ifPresent(entity -> {
            entity.setActivo(false);
            jpaRepository.save(entity);
        });
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
