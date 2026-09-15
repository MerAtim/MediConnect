package com.medconnect.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MedicoJpaRepository extends JpaRepository<MedicoEntity, Long> {

    @Query("SELECT m FROM MedicoEntity m WHERE m.id = :id AND (m.activo = true OR m.activo IS NULL)")
    Optional<MedicoEntity> findActivoById(@Param("id") Long id);

    @Query("SELECT m FROM MedicoEntity m WHERE m.id IN :ids AND (m.activo = true OR m.activo IS NULL)")
    List<MedicoEntity> findActivosByIdIn(@Param("ids") List<Long> ids);

    @Query("SELECT m FROM MedicoEntity m WHERE m.activo = true OR m.activo IS NULL")
    List<MedicoEntity> findAllActivos();

    @Query("SELECT m FROM MedicoEntity m WHERE m.email = :email AND (m.activo = true OR m.activo IS NULL)")
    Optional<MedicoEntity> findActivoByEmail(@Param("email") String email);

    boolean existsByEmailAndActivoFalse(String email);

    // LOW de la re-auditoria e2e (2026-09-08, segunda ronda): "paginacion
    // falsa" -- estas dos reemplazan el patron findAllActivos() + subList en
    // memoria por una consulta SQL real con LIMIT/OFFSET (Pageable) y su
    // COUNT correspondiente. especialidad nullable: "(:especialidad IS NULL
    // OR ...)" cubre listar con y sin filtro en la misma consulta.
    @Query("SELECT m FROM MedicoEntity m WHERE (m.activo = true OR m.activo IS NULL) "
            + "AND (:especialidad IS NULL OR m.especialidad = :especialidad)")
    Page<MedicoEntity> findAllActivos(@Param("especialidad") String especialidad, Pageable pageable);

    @Query("SELECT COUNT(m) FROM MedicoEntity m WHERE (m.activo = true OR m.activo IS NULL) "
            + "AND (:especialidad IS NULL OR m.especialidad = :especialidad)")
    long countActivos(@Param("especialidad") String especialidad);
}
