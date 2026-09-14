package com.medconnect.interfaces.rest;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    // HIGH de la re-auditoria e2e (2026-09-08): Email/Dni (Value Objects,
    // domain.model) validan su propio formato en el constructor y tiran
    // IllegalArgumentException. Los flujos normales de creacion/actualizacion
    // ya validan el formato antes de llegar a construir el VO, pero
    // reconstruir un registro YA persistido con un dato corrupto (legacy,
    // insercion manual, migracion) pasa directo por el constructor sin red
    // de seguridad -- sin este handler, un simple GET o login sobre esa fila
    // tiraba un 500 crudo en vez de un error controlado.
    @Test
    public void handleIllegalArgument_devuelve400ConElMensajeOriginal() {
        ResponseEntity<String> resp = handler.handleIllegalArgument(new IllegalArgumentException("dni invalido: abc"));

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("dni invalido: abc", resp.getBody());
    }

    // LOW de la re-auditoria e2e (2026-09-08, segunda ronda): AesGcmFieldEncryptor.desencriptar()
    // tira IllegalStateException ante un dato cifrado corrupto, truncado, o
    // cifrado con una clave distinta a la actual -- sin este handler, un GET
    // sobre un registro clinico en ese estado devolvia un 500 crudo sin
    // loguear. No es culpa del cliente sino un problema de datos/config del
    // servidor, por eso 500 (no 400) con log a nivel ERROR.
    @Test
    public void handleIllegalState_devuelve500ConElMensajeOriginal() {
        ResponseEntity<String> resp = handler.handleIllegalState(
                new IllegalStateException("No se pudo desencriptar el valor: dato corrupto, truncado o clave incorrecta"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertEquals("No se pudo desencriptar el valor: dato corrupto, truncado o clave incorrecta", resp.getBody());
    }
}
