package com.medconnect.domain.port;

import com.medconnect.domain.model.Medico;
import java.util.List;
import java.util.Optional;

public interface MedicoRepository {

    Medico guardar(Medico medico);

    Optional<Medico> buscarPorId(Long id);

    List<Medico> buscarPorIds(List<Long> ids);

    Optional<Medico> buscarPorEmail(String email);

    // MEDIUM de la re-auditoria e2e (2026-09-08): el UNIQUE(email) de la
    // base es a nivel de toda la tabla (incluye filas soft-deleted), pero
    // buscarPorEmail solo mira activos -- sin este chequeo, crear un
    // medico con el email de un perfil eliminado pasaba la validacion de
    // la app y explotaba en la unique constraint (500 crudo).
    boolean existeEmailEnPerfilEliminado(String email);

    List<Medico> buscarTodos();

    void eliminar(Long id);
}
