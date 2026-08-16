package com.medconnect.application.usecase;

import com.medconnect.domain.model.Usuario;

public interface TokenService {

    String generar(Usuario usuario);
}
