package com.medconnect.domain.port;

import com.medconnect.domain.model.RegistroClinico;
import java.util.List;

public interface RegistroClinicoRepository {

    RegistroClinico guardar(RegistroClinico registro);

    List<RegistroClinico> buscarPorPaciente(Long pacienteId);
}
