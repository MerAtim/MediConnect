package com.medconnect.infrastructure.security;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AesGcmFieldEncryptorTest {

    private static final String CLAVE_VALIDA = Base64.getEncoder().encodeToString(new byte[32]);
    private static final String CLAVE_INSEGURA_POR_DEFECTO = "6i8Bc3Gh3GMVYeFsHdxcB+FEeZ8hiiX0dKIY+8D9v/o=";

    private AesGcmFieldEncryptor encryptor() {
        return new AesGcmFieldEncryptor(CLAVE_VALIDA, false);
    }

    @Test
    public void encriptarYDesencriptar_devuelveElTextoOriginal() {
        AesGcmFieldEncryptor encryptor = encryptor();
        String original = "Fractura de tobillo, indicación de reposo 2 semanas";

        String cifrado = encryptor.encriptar(original);
        assertNotEquals(original, cifrado);
        assertEquals(original, encryptor.desencriptar(cifrado));
    }

    @Test
    public void encriptar_null_devuelveNull() {
        assertNull(encryptor().encriptar(null));
    }

    @Test
    public void desencriptar_null_devuelveNull() {
        assertNull(encryptor().desencriptar(null));
    }

    @Test
    public void encriptar_elMismoTextoDosVeces_daResultadosDistintos() {
        // El IV es aleatorio en cada llamada -- si esto diera el mismo
        // resultado, dos historias clinicas con el mismo diagnostico serian
        // distinguibles por texto cifrado igual sin desencriptar nada, lo
        // que filtra informacion (y, peor, indicaria reuso de IV con GCM).
        AesGcmFieldEncryptor encryptor = encryptor();
        String cifrado1 = encryptor.encriptar("Control de rutina");
        String cifrado2 = encryptor.encriptar("Control de rutina");
        assertNotEquals(cifrado1, cifrado2);
    }

    @Test
    public void desencriptar_datoCorrupto_tiraIllegalState() {
        AesGcmFieldEncryptor encryptor = encryptor();
        String cifrado = encryptor.encriptar("dato sensible");
        // Cambia el ultimo caracter del base64: corrompe el tag de
        // autenticacion de GCM -- debe fallar, no devolver texto basura.
        char ultimo = cifrado.charAt(cifrado.length() - 1);
        char reemplazo = ultimo == 'A' ? 'B' : 'A';
        String corrupto = cifrado.substring(0, cifrado.length() - 1) + reemplazo;

        assertThrows(IllegalStateException.class, () -> encryptor.desencriptar(corrupto));
    }

    @Test
    public void desencriptar_valorMasCortoQueElIv_tiraIllegalState() {
        String demasiadoCorto = Base64.getEncoder().encodeToString(new byte[5]);
        assertThrows(IllegalStateException.class, () -> encryptor().desencriptar(demasiadoCorto));
    }

    @Test
    public void desencriptar_base64Invalido_tiraIllegalState() {
        assertThrows(IllegalStateException.class, () -> encryptor().desencriptar("esto no es base64 valido!!"));
    }

    @Test
    public void constructor_claveNoBase64_tiraIllegalState() {
        assertThrows(IllegalStateException.class, () -> new AesGcmFieldEncryptor("no es base64 ###", false));
    }

    @Test
    public void constructor_claveDeLargoIncorrecto_tiraIllegalState() {
        String clave16Bytes = Base64.getEncoder().encodeToString(new byte[16]);
        assertThrows(IllegalStateException.class, () -> new AesGcmFieldEncryptor(clave16Bytes, false));
    }

    @Test
    public void constructor_cookieSecureConClaveDeDefecto_tiraIllegalState() {
        assertThrows(IllegalStateException.class,
                () -> new AesGcmFieldEncryptor(CLAVE_INSEGURA_POR_DEFECTO, true));
    }

    @Test
    public void constructor_cookieSecureConClavePropia_arrancaBien() {
        AesGcmFieldEncryptor encryptor = new AesGcmFieldEncryptor(CLAVE_VALIDA, true);
        assertEquals("ok", encryptor.desencriptar(encryptor.encriptar("ok")));
    }
}
