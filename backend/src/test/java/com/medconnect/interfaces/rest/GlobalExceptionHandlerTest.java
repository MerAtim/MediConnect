package com.medconnect.interfaces.rest;

import com.medconnect.domain.exception.CredencialesInvalidasException;
import com.medconnect.domain.exception.DemasiadosIntentosException;
import com.medconnect.domain.exception.MedicoInvalidoException;
import com.medconnect.domain.exception.PacienteInvalidoException;
import com.medconnect.domain.exception.RegistroClinicoInvalidoException;
import com.medconnect.domain.exception.TurnoInvalidoException;
import com.medconnect.domain.exception.UsuarioInvalidoException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

// LOW de la re-auditoria e2e (2026-09-08, segunda ronda): "todas las
// respuestas de error devuelven texto plano, mientras el resto de la API
// responde JSON" -- los 9 handlers ahora devuelven ErrorResponse
// ({"message": "..."}) en vez de un String crudo. Antes de este PR solo 2 de
// los 9 handlers tenian test directo; de paso se completa cobertura para
// los 7 restantes.
public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    public void handleTurnoInvalido_devuelve400ConElMensajeOriginal() {
        ResponseEntity<ErrorResponse> resp = handler.handleTurnoInvalido(new TurnoInvalidoException("fechaHora debe ser una fecha futura"));

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("fechaHora debe ser una fecha futura", resp.getBody().getMessage());
    }

    @Test
    public void handleMedicoInvalido_devuelve400ConElMensajeOriginal() {
        ResponseEntity<ErrorResponse> resp = handler.handleMedicoInvalido(new MedicoInvalidoException("nombre es obligatorio"));

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("nombre es obligatorio", resp.getBody().getMessage());
    }

    @Test
    public void handlePacienteInvalido_devuelve400ConElMensajeOriginal() {
        ResponseEntity<ErrorResponse> resp = handler.handlePacienteInvalido(new PacienteInvalidoException("dni es obligatorio"));

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("dni es obligatorio", resp.getBody().getMessage());
    }

    @Test
    public void handleUsuarioInvalido_devuelve400ConElMensajeOriginal() {
        ResponseEntity<ErrorResponse> resp = handler.handleUsuarioInvalido(new UsuarioInvalidoException("ya existe un usuario con ese email"));

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("ya existe un usuario con ese email", resp.getBody().getMessage());
    }

    @Test
    public void handleRegistroClinicoInvalido_devuelve400ConElMensajeOriginal() {
        ResponseEntity<ErrorResponse> resp = handler.handleRegistroClinicoInvalido(new RegistroClinicoInvalidoException("diagnostico es obligatorio"));

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("diagnostico es obligatorio", resp.getBody().getMessage());
    }

    @Test
    public void handleCredencialesInvalidas_devuelve401ConElMensajeOriginal() {
        ResponseEntity<ErrorResponse> resp = handler.handleCredencialesInvalidas(new CredencialesInvalidasException("email o contraseña incorrectos"));

        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        assertEquals("email o contraseña incorrectos", resp.getBody().getMessage());
    }

    @Test
    public void handleDemasiadosIntentos_devuelve429ConElMensajeOriginal() {
        ResponseEntity<ErrorResponse> resp = handler.handleDemasiadosIntentos(new DemasiadosIntentosException("Demasiados intentos fallidos."));

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, resp.getStatusCode());
        assertEquals("Demasiados intentos fallidos.", resp.getBody().getMessage());
    }

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
        ResponseEntity<ErrorResponse> resp = handler.handleIllegalArgument(new IllegalArgumentException("dni invalido: abc"));

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("dni invalido: abc", resp.getBody().getMessage());
    }

    // LOW de la re-auditoria e2e (2026-09-08, segunda ronda): AesGcmFieldEncryptor.desencriptar()
    // tira IllegalStateException ante un dato cifrado corrupto, truncado, o
    // cifrado con una clave distinta a la actual -- sin este handler, un GET
    // sobre un registro clinico en ese estado devolvia un 500 crudo sin
    // loguear. No es culpa del cliente sino un problema de datos/config del
    // servidor, por eso 500 (no 400) con log a nivel ERROR.
    @Test
    public void handleIllegalState_devuelve500ConElMensajeOriginal() {
        ResponseEntity<ErrorResponse> resp = handler.handleIllegalState(
                new IllegalStateException("No se pudo desencriptar el valor: dato corrupto, truncado o clave incorrecta"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertEquals("No se pudo desencriptar el valor: dato corrupto, truncado o clave incorrecta", resp.getBody().getMessage());
    }
}
