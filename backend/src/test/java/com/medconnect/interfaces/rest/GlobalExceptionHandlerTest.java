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
}
