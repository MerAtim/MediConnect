-- LOW de la re-auditoria e2e (2026-09-08, segunda ronda): "email/nombre" no
-- tenian NOT NULL a nivel de esquema, la integridad dependia solo de la
-- validacion en la capa de aplicacion (CreateMedicoRequest.validar(),
-- CreatePacienteRequest.validar(), RegistrarUsuarioService.guardar()) y del
-- Value Object Email en el dominio.
--
-- Importante: medicos.email y pacientes.email quedan deliberadamente
-- nullable -- un Medico/Paciente puede existir sin cuenta de acceso
-- vinculada (decision explicita de HIGH #8 de la re-auditoria: Usuario y
-- Medico/Paciente estan desacoplados a proposito). Solo se endurece lo que
-- ya es obligatorio en todos los flujos de creacion/actualizacion reales:
-- el nombre de medico/paciente/usuario, y el email de usuario (una cuenta
-- de acceso sin email no tiene forma de loguearse).

ALTER TABLE medicos ALTER COLUMN nombre SET NOT NULL;
ALTER TABLE pacientes ALTER COLUMN nombre SET NOT NULL;
ALTER TABLE usuarios ALTER COLUMN nombre SET NOT NULL;
ALTER TABLE usuarios ALTER COLUMN email SET NOT NULL;
