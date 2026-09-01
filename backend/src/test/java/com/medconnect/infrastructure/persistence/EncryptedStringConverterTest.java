package com.medconnect.infrastructure.persistence;

import com.medconnect.infrastructure.security.AesGcmFieldEncryptor;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class EncryptedStringConverterTest {

    private final AesGcmFieldEncryptor encryptor =
            new AesGcmFieldEncryptor(Base64.getEncoder().encodeToString(new byte[32]), false);
    private final EncryptedStringConverter converter = new EncryptedStringConverter(encryptor);

    @Test
    public void convertToDatabaseColumn_yConvertToEntityAttribute_sonRoundTrip() {
        String columna = converter.convertToDatabaseColumn("Diagnóstico confidencial");
        assertEquals("Diagnóstico confidencial", converter.convertToEntityAttribute(columna));
    }

    @Test
    public void convertToDatabaseColumn_null_devuelveNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    public void convertToEntityAttribute_null_devuelveNull() {
        assertNull(converter.convertToEntityAttribute(null));
    }
}
