package com.medconnect.domain.port;

import com.medconnect.domain.model.Paciente;
import java.util.List;
import java.util.Optional;

public interface PacienteRepository {

    Paciente guardar(Paciente paciente);

    Optional<Paciente> buscarPorId(Long id);

    List<Paciente> buscarPorIds(List<Long> ids);

    Optional<Paciente> buscarPorEmail(String email);

    // MEDIUM de la re-auditoria e2e (2026-09-08): el UNIQUE(email) de la
    // base es a nivel de toda la tabla (incluye filas soft-deleted), pero
    // buscarPorEmail solo mira activos -- sin este chequeo, crear un
    // paciente con el email de un perfil eliminado pasaba la validacion de
    // la app y explotaba en la unique constraint (500 crudo).
    boolean existeEmailEnPerfilEliminado(String email);

    List<Paciente> buscarTodos();

    void eliminar(Long id);
}
