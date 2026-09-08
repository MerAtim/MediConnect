package com.medconnect.interfaces.rest;

import com.medconnect.domain.exception.CredencialesInvalidasException;
import com.medconnect.domain.exception.DemasiadosIntentosException;
import com.medconnect.domain.exception.MedicoInvalidoException;
import com.medconnect.domain.exception.PacienteInvalidoException;
import com.medconnect.domain.exception.RegistroClinicoInvalidoException;
import com.medconnect.domain.exception.TurnoInvalidoException;
import com.medconnect.domain.exception.UsuarioInvalidoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TurnoInvalidoException.class)
    public ResponseEntity<String> handleTurnoInvalido(TurnoInvalidoException ex) {
        log.warn("Turno invalido: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(MedicoInvalidoException.class)
    public ResponseEntity<String> handleMedicoInvalido(MedicoInvalidoException ex) {
        log.warn("Medico invalido: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(PacienteInvalidoException.class)
    public ResponseEntity<String> handlePacienteInvalido(PacienteInvalidoException ex) {
        log.warn("Paciente invalido: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(UsuarioInvalidoException.class)
    public ResponseEntity<String> handleUsuarioInvalido(UsuarioInvalidoException ex) {
        log.warn("Usuario invalido: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(RegistroClinicoInvalidoException.class)
    public ResponseEntity<String> handleRegistroClinicoInvalido(RegistroClinicoInvalidoException ex) {
        log.warn("Registro clinico invalido: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<String> handleCredencialesInvalidas(CredencialesInvalidasException ex) {
        log.warn("Intento de login con credenciales invalidas");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(DemasiadosIntentosException.class)
    public ResponseEntity<String> handleDemasiadosIntentos(DemasiadosIntentosException ex) {
        log.warn("Login bloqueado por rate limit: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ex.getMessage());
    }

    // Red de seguridad para los Value Objects del dominio (Email, Dni):
    // validan su propio formato en el constructor y tiran esta excepcion.
    // En los flujos normales de creacion/actualizacion el formato ya se
    // valida antes de llegar aca (ver MedicoFactory/PacienteFactory/
    // RegistrarUsuarioService), asi que este handler solo entra en juego
    // si un dato YA persistido esta mal formado (legacy, insercion manual,
    // migracion) -- sin esto, reconstruir ese registro en un simple GET o
    // login tiraba un 500 crudo en vez de un error controlado.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Argumento invalido (posible dato corrupto en la base): {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
