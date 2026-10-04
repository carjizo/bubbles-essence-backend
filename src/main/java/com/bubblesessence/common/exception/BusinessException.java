package com.bubblesessence.common.exception;

/**
 * Se lanza cuando una operación viola una regla de negocio
 * (ej. nombre duplicado, referencia inactiva, etc.).
 * El GlobalExceptionHandler la traduce a HTTP 409 (Conflict).
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String mensaje) {
        super(mensaje);
    }
}
