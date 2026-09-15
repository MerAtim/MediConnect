package com.medconnect.interfaces.rest;

// LOW de la re-auditoria e2e (2026-09-08, segunda ronda): "todas las
// respuestas de error devuelven texto plano, mientras el resto de la API
// responde JSON" -- GlobalExceptionHandler devolvia ResponseEntity<String>
// (Content-Type: text/plain), inconsistente con el resto de los endpoints
// (JSON). El campo se llama "message" a proposito: utils.js
// (readErrorMessage) ya intentaba parsear el body como JSON y leer
// json?.message como fallback desde antes de este cambio, asi que el
// frontend no necesito ningun ajuste para este PR.
public class ErrorResponse {

    private String message;

    public ErrorResponse() {}

    public ErrorResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
