package com.encurtador_url.SuperApp.exception;

/**
 * Exceção para URL expirada
 */
public class UrlExpiredException extends RuntimeException {
    public UrlExpiredException(String message) {
        super(message);
    }
}

