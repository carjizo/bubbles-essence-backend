package com.bubblesessence.common.exception;

/**
 * Login incorrecto (usuario no existe, inactivo, o clave incorrecta).
 * El GlobalExceptionHandler la traduce a HTTP 401 (Unauthorized).
 * Separada de BusinessException (409) porque semánticamente son casos
 * distintos: acá no hay conflicto de datos, es que no pudo autenticarse.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String mensaje) {
        super(mensaje);
    }
}
