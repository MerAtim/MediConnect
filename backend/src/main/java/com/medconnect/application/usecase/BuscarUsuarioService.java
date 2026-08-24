package com.medconnect.application.usecase;

import com.medconnect.domain.model.Usuario;
import com.medconnect.domain.port.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuscarUsuarioService implements BuscarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;

    public BuscarUsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<Usuario> buscarTodos() {
        return usuarioRepository.buscarTodos();
    }
}
