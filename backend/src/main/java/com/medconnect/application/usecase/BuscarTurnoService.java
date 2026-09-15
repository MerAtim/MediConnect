package com.medconnect.application.usecase;

import com.medconnect.domain.model.Turno;
import com.medconnect.domain.port.TurnoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BuscarTurnoService implements BuscarTurnoUseCase {

    private final TurnoRepository turnoRepository;

    public BuscarTurnoService(TurnoRepository turnoRepository) {
        this.turnoRepository = turnoRepository;
    }

    @Override
    public Optional<Turno> buscarPorId(Long id) {
        return turnoRepository.buscarPorId(id);
    }

    @Override
    public List<Turno> buscarPorMedico(Long medicoId) {
        return turnoRepository.buscarPorMedico(medicoId);
    }

    @Override
    public List<Turno> buscarPorPaciente(Long pacienteId) {
        return turnoRepository.buscarPorPaciente(pacienteId);
    }

    @Override
    public List<Turno> buscarTodos() {
        return turnoRepository.buscarTodos();
    }

    @Override
    public List<Turno> buscarPaginaPorMedico(Long medicoId, int page, int size) {
        return turnoRepository.buscarPaginaPorMedico(medicoId, page, size);
    }

    @Override
    public long contarPorMedico(Long medicoId) {
        return turnoRepository.contarPorMedico(medicoId);
    }

    @Override
    public List<Turno> buscarPaginaPorPaciente(Long pacienteId, int page, int size) {
        return turnoRepository.buscarPaginaPorPaciente(pacienteId, page, size);
    }

    @Override
    public long contarPorPaciente(Long pacienteId) {
        return turnoRepository.contarPorPaciente(pacienteId);
    }

    @Override
    public List<Turno> buscarPagina(int page, int size) {
        return turnoRepository.buscarPagina(page, size);
    }

    @Override
    public long contar() {
        return turnoRepository.contar();
    }
}
