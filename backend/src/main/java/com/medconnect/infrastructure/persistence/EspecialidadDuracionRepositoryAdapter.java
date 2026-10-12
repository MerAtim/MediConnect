package com.medconnect.infrastructure.persistence;

import com.medconnect.domain.port.EspecialidadDuracionRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.stream.Collectors;

@Repository
@Profile("!test")
public class EspecialidadDuracionRepositoryAdapter implements EspecialidadDuracionRepository {

    private final EspecialidadDuracionJpaRepository jpaRepository;

    public EspecialidadDuracionRepositoryAdapter(EspecialidadDuracionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Map<String, Integer> buscarTodas() {
        return jpaRepository.findAll().stream()
                .collect(Collectors.toMap(EspecialidadDuracionEntity::getEspecialidad, EspecialidadDuracionEntity::getDuracionMinutos));
    }

    @Override
    public int buscarDuracion(String especialidad) {
        return jpaRepository.findById(especialidad)
                .map(EspecialidadDuracionEntity::getDuracionMinutos)
                .orElse(DURACION_DEFAULT_MINUTOS);
    }

    @Override
    public void establecer(String especialidad, int minutos) {
        jpaRepository.save(new EspecialidadDuracionEntity(especialidad, minutos));
    }
}
