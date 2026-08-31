package com.medconnect.domain.port;

import com.medconnect.domain.model.Medico;
import java.util.List;
import java.util.Optional;

public interface MedicoRepository {

    Medico guardar(Medico medico);

    Optional<Medico> buscarPorId(Long id);

    List<Medico> buscarPorIds(List<Long> ids);

    Optional<Medico> buscarPorEmail(String email);

    List<Medico> buscarTodos();

    void eliminar(Long id);
}
