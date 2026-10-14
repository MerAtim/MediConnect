-- La unique constraint original (medico_id, fecha_hora) sigue vigente para
-- turnos activos (red de seguridad contra la carrera de dos requests
-- concurrentes reservando el mismo horario exacto), pero antes tambien
-- bloqueaba reutilizar un horario cuyo unico ocupante ya estaba CANCELADO --
-- inconsistente con CrearTurnoService, que a nivel aplicacion ya excluye los
-- turnos CANCELADO del chequeo de solapamiento.
ALTER TABLE turnos DROP CONSTRAINT uk_turnos_medico_fecha;

CREATE UNIQUE INDEX uk_turnos_medico_fecha ON turnos (medico_id, fecha_hora) WHERE estado <> 'CANCELADO';
