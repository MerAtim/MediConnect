package com.medconnect.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PacienteJpaRepository extends JpaRepository<PacienteEntity, Long> {

    @Query("SELECT p FROM PacienteEntity p WHERE p.id = :id AND (p.activo = true OR p.activo IS NULL)")
    Optional<PacienteEntity> findActivoById(@Param("id") Long id);

    @Query("SELECT p FROM PacienteEntity p WHERE p.id IN :ids AND (p.activo = true OR p.activo IS NULL)")
    List<PacienteEntity> findActivosByIdIn(@Param("ids") List<Long> ids);

    @Query("SELECT p FROM PacienteEntity p WHERE p.activo = true OR p.activo IS NULL")
    List<PacienteEntity> findAllActivos();

    @Query("SELECT p FROM PacienteEntity p WHERE p.email = :email AND (p.activo = true OR p.activo IS NULL)")
    Optional<PacienteEntity> findActivoByEmail(@Param("email") String email);

    boolean existsByEmailAndActivoFalse(String email);

    // LOW de la re-auditoria e2e (2026-09-08, segunda ronda): "paginacion
    // falsa" -- reemplaza findAllActivos() + subList en memoria por una
    // consulta SQL real con LIMIT/OFFSET (Pageable) y su COUNT.
    @Query("SELECT p FROM PacienteEntity p WHERE p.activo = true OR p.activo IS NULL")
    Page<PacienteEntity> findAllActivos(Pageable pageable);

    @Query("SELECT COUNT(p) FROM PacienteEntity p WHERE p.activo = true OR p.activo IS NULL")
    long countActivos();
}
