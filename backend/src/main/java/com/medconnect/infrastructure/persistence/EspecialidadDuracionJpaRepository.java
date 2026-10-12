package com.medconnect.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

// El Id (especialidad, clave natural) siempre viene asignado -- nunca es
// null, asi que JpaRepository.save() hace merge() en vez de persist(), y
// merge() con un id que no existe todavia en la tabla inserta la fila
// (upsert real, sin necesitar un SELECT previo en el adapter).
public interface EspecialidadDuracionJpaRepository extends JpaRepository<EspecialidadDuracionEntity, String> {
}
