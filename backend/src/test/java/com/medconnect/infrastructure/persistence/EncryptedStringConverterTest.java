package com.medconnect.infrastructure.persistence;

import com.medconnect.infrastructure.security.AesGcmFieldEncryptor;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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

    // LOW de la re-auditoria e2e (2026-09-08, segunda ronda): el test de
    // arriba solo prueba el round-trip (desencriptar(encriptar(x)) == x); si
    // el converter se volviera un no-op (bug de config, ej. cifrado
    // deshabilitado por error), ese test seguiria pasando igual. Este test
    // confirma ademas que lo que efectivamente queda en la columna NO es el
    // texto plano original.
    @Test
    public void convertToDatabaseColumn_noDevuelveElTextoPlanoOriginal() {
        String columna = converter.convertToDatabaseColumn("Diagnóstico confidencial");
        assertNotEquals("Diagnóstico confidencial", columna);
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
