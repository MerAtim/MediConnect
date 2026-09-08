-- Indices para las columnas paciente_id que se filtran directamente en
-- consultas reales (TurnoJpaRepository.findByPacienteId,
-- RegistroClinicoJpaRepository.findByPacienteIdOrderByFechaDesc), hoy sin
-- indice: cada llamada hace un seq scan completo de la tabla.
--
-- turnos.medico_id y registros_clinicos.medico_id quedan afuera a proposito:
-- - turnos.medico_id ya esta cubierto por uk_turnos_medico_fecha
--   (medico_id, fecha_hora) como columna lider del indice compuesto.
-- - registros_clinicos.medico_id no se filtra en ningun query del repo
--   (solo se guarda/muestra), agregar un indice ahi seria especular sobre
--   un uso que todavia no existe.

CREATE INDEX idx_turnos_paciente_id ON turnos (paciente_id);

-- Compuesto (no solo paciente_id) porque el unico query real ordena por
-- fecha desc ademas de filtrar por paciente: el indice cubre filtro + orden
-- en un solo scan, no hace falta un sort aparte.
CREATE INDEX idx_registros_clinicos_paciente_fecha ON registros_clinicos (paciente_id, fecha DESC);
