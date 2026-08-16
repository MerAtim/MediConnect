package com.medconnect.interfaces.rest;

import com.medconnect.domain.exception.MedicoInvalidoException;
import com.medconnect.domain.exception.TurnoInvalidoException;
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
}
