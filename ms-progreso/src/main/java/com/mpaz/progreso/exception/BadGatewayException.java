package com.mpaz.progreso.exception;

/** Un servicio del que depende ms-admin no respondió (o respondió con error). */
public class BadGatewayException extends RuntimeException {
    public BadGatewayException(String message) {
        super(message);
    }
}
