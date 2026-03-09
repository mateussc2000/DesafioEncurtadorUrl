package com.encurtador_url.SuperApp.exception;

/**
 * Exceção para URL inválida
 */
public class InvalidUrlException extends RuntimeException {
    public InvalidUrlException(String message) {
        super(message);
    }

    public InvalidUrlException(String message, Throwable cause) {
        super(message, cause);
    }
}

