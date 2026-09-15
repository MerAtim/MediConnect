package com.medconnect.application.usecase;

import com.medconnect.domain.model.Usuario;

import java.util.List;

public interface BuscarUsuarioUseCase {

    List<Usuario> buscarTodos();

    List<Usuario> buscarPagina(int page, int size);

    long contar();
}
