package com.medconnect.interfaces.rest;

import com.medconnect.domain.exception.CredencialesInvalidasException;
import com.medconnect.domain.exception.MedicoInvalidoException;
import com.medconnect.domain.exception.PacienteInvalidoException;
import com.medconnect.domain.exception.RegistroClinicoInvalidoException;
import com.medconnect.domain.exception.TurnoInvalidoException;
import com.medconnect.domain.exception.UsuarioInvalidoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TurnoInvalidoException.class)
    public ResponseEntity<String> handleTurnoInvalido(TurnoInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(MedicoInvalidoException.class)
    public ResponseEntity<String> handleMedicoInvalido(MedicoInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(PacienteInvalidoException.class)
    public ResponseEntity<String> handlePacienteInvalido(PacienteInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(UsuarioInvalidoException.class)
    public ResponseEntity<String> handleUsuarioInvalido(UsuarioInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(RegistroClinicoInvalidoException.class)
    public ResponseEntity<String> handleRegistroClinicoInvalido(RegistroClinicoInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<String> handleCredencialesInvalidas(CredencialesInvalidasException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }
}
