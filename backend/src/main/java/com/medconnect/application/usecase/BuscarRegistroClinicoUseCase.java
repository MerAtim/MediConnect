package com.medconnect.application.usecase;

import com.medconnect.domain.model.RegistroClinico;
import java.util.List;

public interface BuscarRegistroClinicoUseCase {
    List<RegistroClinico> buscarPorPaciente(Long pacienteId);
}
