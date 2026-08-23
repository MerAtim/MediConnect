package com.medconnect.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistroClinicoJpaRepository extends JpaRepository<RegistroClinicoEntity, Long> {

    List<RegistroClinicoEntity> findByPacienteIdOrderByFechaDesc(Long pacienteId);
}
