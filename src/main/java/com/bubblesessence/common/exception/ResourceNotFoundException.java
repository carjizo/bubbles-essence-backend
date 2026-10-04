package com.bubblesessence.common.exception;

/**
 * Se lanza cuando se busca una entidad por id y no existe.
 * El GlobalExceptionHandler la traduce a HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String entidad, Object id) {
        super("%s con id %s no fue encontrado".formatted(entidad, id));
    }

    public ResourceNotFoundException(String mensaje) {
        super(mensaje);
    }
}
