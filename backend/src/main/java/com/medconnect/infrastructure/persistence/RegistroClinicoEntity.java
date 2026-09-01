package com.medconnect.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "registros_clinicos")
public class RegistroClinicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fecha;
    private Long medicoId;
    private Long pacienteId;

    // Cifradas at-rest (AES-256-GCM, ver AesGcmFieldEncryptor): es contenido
    // médico real, la única tabla del proyecto que guarda algo así. columnDefinition=TEXT
    // porque el texto cifrado+IV+tag en base64 pesa ~1.4x el original y ya
    // no entra en un VARCHAR(255) salvo para textos muy cortos.
    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private String diagnostico;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private String tratamiento;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private String observaciones;

    protected RegistroClinicoEntity() {}

    public RegistroClinicoEntity(Long id, LocalDateTime fecha, Long medicoId, Long pacienteId, String diagnostico, String tratamiento, String observaciones) {
        this.id = id;
        this.fecha = fecha;
        this.medicoId = medicoId;
        this.pacienteId = pacienteId;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.observaciones = observaciones;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public Long getMedicoId() {
        return medicoId;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public String getTratamiento() {
        return tratamiento;
    }

    public String getObservaciones() {
        return observaciones;
    }
}
