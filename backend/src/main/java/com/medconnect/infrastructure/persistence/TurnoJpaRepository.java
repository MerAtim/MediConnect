package com.medconnect.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TurnoJpaRepository extends JpaRepository<TurnoEntity, Long> {

    List<TurnoEntity> findByMedicoId(Long medicoId);

    List<TurnoEntity> findByPacienteId(Long pacienteId);
}
