package com.medconnect.domain.exception;

// LOW de la re-auditoria e2e (2026-09-08, segunda ronda): las 7 excepciones
// de este paquete eran identicas letra por letra (extendian RuntimeException,
// un unico constructor con message), sin una clase base comun. No afectaba el
// comportamiento -- GlobalExceptionHandler ya las mapea una por una -- pero
// al agregar una nueva excepcion de dominio era facil olvidar registrar su
// @ExceptionHandler, y no habia forma de capturarlas todas genericamente si
// hiciera falta.
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
