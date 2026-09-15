package com.medconnect.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TurnoJpaRepository extends JpaRepository<TurnoEntity, Long> {

    List<TurnoEntity> findByMedicoId(Long medicoId);

    List<TurnoEntity> findByPacienteId(Long pacienteId);

    // LOW de la re-auditoria e2e (2026-09-08, segunda ronda): "paginacion
    // falsa" -- variantes con Pageable de las dos consultas de arriba, para
    // GET /turnos con LIMIT/OFFSET real. El "ver todos" (sin filtro) usa
    // findAll(Pageable)/count() que JpaRepository ya trae heredados, no
    // hace falta declarar nada aca para ese caso.
    Page<TurnoEntity> findByMedicoId(Long medicoId, Pageable pageable);

    long countByMedicoId(Long medicoId);

    Page<TurnoEntity> findByPacienteId(Long pacienteId, Pageable pageable);

    long countByPacienteId(Long pacienteId);
}
