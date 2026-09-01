package com.medconnect.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

// Cifrado a nivel de campo (AES-256-GCM) para contenido sensible que se
// persiste en columnas de texto -- hoy solo lo usa EncryptedStringConverter
// para diagnostico/tratamiento/observaciones de RegistroClinico, la unica
// tabla del proyecto que guarda contenido medico real.
//
// Formato de lo que se guarda en la base: base64(IV de 12 bytes || texto
// cifrado || tag de autenticacion GCM de 16 bytes). El IV es aleatorio en
// cada llamada a encriptar() y viaja junto al resto porque GCM lo necesita
// para desencriptar y no hace falta que sea secreto, solo unico por
// mensaje -- reusar un IV con la misma clave es lo que rompe la seguridad
// de GCM, nunca pasa aca porque se genera de nuevo en cada encriptar().
@Component
public class AesGcmFieldEncryptor {

    private static final String CLAVE_INSEGURA_POR_DEFECTO = "6i8Bc3Gh3GMVYeFsHdxcB+FEeZ8hiiX0dKIY+8D9v/o=";
    private static final String ALGORITMO_CIFRADO = "AES/GCM/NoPadding";
    private static final int TAMANO_IV_BYTES = 12;
    private static final int TAMANO_TAG_BITS = 128;

    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();

    public AesGcmFieldEncryptor(@Value("${app.encryption-key}") String encryptionKeyBase64,
                                 @Value("${app.cookie-secure}") boolean cookieSecure) {
        // Mismo criterio que JwtTokenService: cookie-secure=true es la señal de
        // que esto es un despliegue real, no un mvnw local -- ahi no se puede
        // arrancar con la clave de desarrollo hardcodeada en el repo, o
        // cualquiera con acceso al codigo podria desencriptar las historias
        // clinicas de produccion.
        if (cookieSecure && CLAVE_INSEGURA_POR_DEFECTO.equals(encryptionKeyBase64)) {
            throw new IllegalStateException(
                    "ENCRYPTION_KEY no fue configurado: no se puede arrancar con la clave de desarrollo "
                            + "por defecto cuando COOKIE_SECURE=true (despliegue real).");
        }
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(encryptionKeyBase64);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("ENCRYPTION_KEY debe estar codificado en Base64.", e);
        }
        if (keyBytes.length != 32) {
            throw new IllegalStateException(
                    "ENCRYPTION_KEY debe decodificar a 32 bytes (AES-256); se recibieron "
                            + keyBytes.length + ". Generar uno nuevo con: openssl rand -base64 32");
        }
        this.key = new SecretKeySpec(keyBytes, "AES");
    }

    public String encriptar(String textoPlano) {
        if (textoPlano == null) {
            return null;
        }
        try {
            byte[] iv = new byte[TAMANO_IV_BYTES];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALGORITMO_CIFRADO);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAMANO_TAG_BITS, iv));
            byte[] cifrado = cipher.doFinal(textoPlano.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + cifrado.length);
            buffer.put(iv).put(cifrado);
            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo encriptar el valor", e);
        }
    }

    public String desencriptar(String valorAlmacenado) {
        if (valorAlmacenado == null) {
            return null;
        }
        try {
            byte[] datos = Base64.getDecoder().decode(valorAlmacenado);
            if (datos.length < TAMANO_IV_BYTES) {
                // Arrays.copyOfRange no tira excepcion por si sola con un
                // input mas corto que el IV -- rellena con ceros en vez de
                // fallar. Se valida a mano para no terminar desencriptando
                // "silenciosamente" con un IV mitad real mitad relleno.
                throw new IllegalArgumentException(
                        "El valor almacenado mide " + datos.length + " bytes, menos que el IV de "
                                + TAMANO_IV_BYTES + " bytes");
            }
            byte[] iv = Arrays.copyOfRange(datos, 0, TAMANO_IV_BYTES);
            byte[] cifrado = Arrays.copyOfRange(datos, TAMANO_IV_BYTES, datos.length);

            Cipher cipher = Cipher.getInstance(ALGORITMO_CIFRADO);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAMANO_TAG_BITS, iv));
            byte[] textoPlano = cipher.doFinal(cifrado);
            return new String(textoPlano, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            // IllegalArgumentException cubre un Base64 invalido, el chequeo
            // de longitud de arriba, y (via GCM) el tag de autenticacion no
            // coincidiendo -- en todos los casos el valor almacenado no es
            // algo que este componente haya encriptado con esta clave.
            throw new IllegalStateException(
                    "No se pudo desencriptar el valor: dato corrupto, truncado o clave incorrecta", e);
        }
    }
}
