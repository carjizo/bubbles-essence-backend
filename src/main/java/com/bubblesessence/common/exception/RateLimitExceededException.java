package com.bubblesessence.common.exception;

/**
 * Se lanza cuando una IP supera el límite de requests permitido en la
 * ventana de tiempo. El GlobalExceptionHandler la traduce a HTTP 429
 * (Too Many Requests), el código estándar para esto.
 */
public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String mensaje) {
        super(mensaje);
    }
}

