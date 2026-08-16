package com.medconnect.domain.port;

import com.medconnect.domain.model.Usuario;
import java.util.Optional;

public interface UsuarioRepository {

    Usuario guardar(Usuario usuario);

    Optional<Usuario> buscarPorEmail(String email);
}
