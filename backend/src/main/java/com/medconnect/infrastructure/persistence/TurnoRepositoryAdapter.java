package com.medconnect.infrastructure.persistence;

import com.medconnect.domain.exception.TurnoInvalidoException;
import com.medconnect.domain.model.Medico;
import com.medconnect.domain.model.Paciente;
import com.medconnect.domain.model.Turno;
import com.medconnect.domain.port.TurnoRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile("!test")
public class TurnoRepositoryAdapter implements TurnoRepository {

    private final TurnoJpaRepository jpaRepository;

    public TurnoRepositoryAdapter(TurnoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Turno guardar(Turno turno) {
        TurnoEntity entity = new TurnoEntity(
                turno.getId(),
                turno.getFechaHora(),
                turno.getEspecialidad(),
                turno.getMedico() != null ? turno.getMedico().getId() : null,
                turno.getPaciente() != null ? turno.getPaciente().getId() : null,
                turno.getEstado(),
                turno.getPreparacion()
        );
        TurnoEntity guardado;
        // Traduce la excepcion de infraestructura (constraint unique(medico_id,
        // fecha_hora) que absorbe la carrera de dos requests concurrentes
        // reservando el mismo horario) a una excepcion de dominio aca, en el
        // adapter -- antes este try/catch vivia en CrearTurnoService
        // (application.usecase), que terminaba dependiendo de un tipo de
        // Spring Data. La capa de aplicacion no deberia conocer excepciones de
        // infraestructura; el adapter es el unico lado con permiso de
        // depender de ambas.
        try {
            guardado = jpaRepository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            throw new TurnoInvalidoException("El médico no está disponible en la fecha y hora solicitada");
        }
        turno.setId(guardado.getId());
        return turno;
    }

    @Override
    public Optional<Turno> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Turno> buscarPorMedico(Long medicoId) {
        return jpaRepository.findByMedicoId(medicoId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Turno> buscarPorPaciente(Long pacienteId) {
        return jpaRepository.findByPacienteId(pacienteId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Turno> buscarTodos() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Turno> buscarPaginaPorMedico(Long medicoId, int page, int size) {
        int paginaSegura = Math.max(page, 0);
        int tamanioSeguro = Math.max(size, 1);
        return jpaRepository.findByMedicoId(medicoId, PageRequest.of(paginaSegura, tamanioSeguro))
                .getContent().stream().map(this::toDomain).toList();
    }

    @Override
    public long contarPorMedico(Long medicoId) {
        return jpaRepository.countByMedicoId(medicoId);
    }

    @Override
    public List<Turno> buscarPaginaPorPaciente(Long pacienteId, int page, int size) {
        int paginaSegura = Math.max(page, 0);
        int tamanioSeguro = Math.max(size, 1);
        return jpaRepository.findByPacienteId(pacienteId, PageRequest.of(paginaSegura, tamanioSeguro))
                .getContent().stream().map(this::toDomain).toList();
    }

    @Override
    public long contarPorPaciente(Long pacienteId) {
        return jpaRepository.countByPacienteId(pacienteId);
    }

    @Override
    public List<Turno> buscarPagina(int page, int size) {
        int paginaSegura = Math.max(page, 0);
        int tamanioSeguro = Math.max(size, 1);
        return jpaRepository.findAll(PageRequest.of(paginaSegura, tamanioSeguro))
                .getContent().stream().map(this::toDomain).toList();
    }

    @Override
    public long contar() {
        return jpaRepository.count();
    }

    private Turno toDomain(TurnoEntity entity) {
        Medico medico = entity.getMedicoId() != null ? Medico.conId(entity.getMedicoId()) : null;
        Paciente paciente = entity.getPacienteId() != null ? Paciente.conId(entity.getPacienteId()) : null;
        Turno turno = new Turno(entity.getId(), entity.getFechaHora(), entity.getEspecialidad(), medico, paciente, entity.getEstado());
        turno.setPreparacion(entity.getPreparacion());
        return turno;
    }
}
