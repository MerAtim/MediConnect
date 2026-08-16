package com.medconnect.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MedicoJpaRepository extends JpaRepository<MedicoEntity, Long> {

    @Query("SELECT m FROM MedicoEntity m WHERE m.id = :id AND (m.activo = true OR m.activo IS NULL)")
    Optional<MedicoEntity> findActivoById(@Param("id") Long id);

    @Query("SELECT m FROM MedicoEntity m WHERE m.activo = true OR m.activo IS NULL")
    List<MedicoEntity> findAllActivos();
}
