package com.medconnect.domain.port;

import com.medconnect.domain.model.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {

    Usuario guardar(Usuario usuario);

    Optional<Usuario> buscarPorEmail(String email);

    Optional<Usuario> buscarPorId(Long id);

    List<Usuario> buscarTodos();

    // LOW de la re-auditoria e2e (2026-09-08, segunda ronda): "paginacion
    // falsa" -- buscarTodos() de arriba sigue existiendo para el pool de
    // "cuenta vinculada" del frontend (necesita la lista completa).
    // buscarPagina/contar son para el listado paginado real de la tabla
    // Usuarios: una consulta SQL con LIMIT/OFFSET, no un subList en memoria.
    List<Usuario> buscarPagina(int page, int size);

    long contar();
}
