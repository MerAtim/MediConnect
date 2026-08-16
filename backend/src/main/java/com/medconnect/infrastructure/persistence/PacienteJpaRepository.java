package com.medconnect.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PacienteJpaRepository extends JpaRepository<PacienteEntity, Long> {

    @Query("SELECT p FROM PacienteEntity p WHERE p.id = :id AND (p.activo = true OR p.activo IS NULL)")
    Optional<PacienteEntity> findActivoById(@Param("id") Long id);

    @Query("SELECT p FROM PacienteEntity p WHERE p.activo = true OR p.activo IS NULL")
    List<PacienteEntity> findAllActivos();
}
