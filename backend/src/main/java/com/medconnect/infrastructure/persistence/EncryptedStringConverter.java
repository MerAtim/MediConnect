package com.medconnect.infrastructure.persistence;

import com.medconnect.infrastructure.security.AesGcmFieldEncryptor;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

// autoApply=false a proposito: se aplica campo por campo con @Convert
// (ver RegistroClinicoEntity) en vez de a cualquier String de cualquier
// entidad -- cifrar, por ejemplo, el email de Usuario rompería
// buscarPorEmail() (que filtra por igualdad en la base).
@Converter(autoApply = false)
@Component
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private final AesGcmFieldEncryptor encryptor;

    public EncryptedStringConverter(AesGcmFieldEncryptor encryptor) {
        this.encryptor = encryptor;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return encryptor.encriptar(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return encryptor.desencriptar(dbData);
    }
}
