package com.medconnect.domain.port;

import com.medconnect.domain.model.Turno;
import java.util.List;
import java.util.Optional;

public interface TurnoRepository {

    Turno guardar(Turno turno);

    Optional<Turno> buscarPorId(Long id);

    List<Turno> buscarPorMedico(Long medicoId);

    List<Turno> buscarPorPaciente(Long pacienteId);

    List<Turno> buscarTodos();

    // LOW de la re-auditoria e2e (2026-09-08, segunda ronda): "paginacion
    // falsa" -- las variantes de arriba (buscarPorMedico/buscarPorPaciente/
    // buscarTodos) siguen existiendo para los usos que necesitan la lista
    // completa (ej. TurnoTest, chequeos de solapamiento/turnos activos).
    // Las de abajo son para GET /turnos: una consulta SQL con LIMIT/OFFSET,
    // cubren las 5 ramas del endpoint (medico ve los suyos, paciente ve los
    // suyos, admin filtra por medicoId/pacienteId, o ve todos) porque esas
    // ramas se reducen a estas 3 consultas subyacentes.
    List<Turno> buscarPaginaPorMedico(Long medicoId, int page, int size);

    long contarPorMedico(Long medicoId);

    List<Turno> buscarPaginaPorPaciente(Long pacienteId, int page, int size);

    long contarPorPaciente(Long pacienteId);

    List<Turno> buscarPagina(int page, int size);

    long contar();
}