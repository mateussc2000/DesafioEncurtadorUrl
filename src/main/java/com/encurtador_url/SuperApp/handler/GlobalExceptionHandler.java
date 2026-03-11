package com.encurtador_url.SuperApp.handler;

import com.encurtador_url.SuperApp.dto.response.ErrorResponse;
import com.encurtador_url.SuperApp.exception.UrlInvalidaExceptionException;
import com.encurtador_url.SuperApp.exception.UrlExpiredException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * Handler global para exceções da API
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Trata InvalidUrlException
     */
    @ExceptionHandler(UrlInvalidaExceptionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUrlException(
            UrlInvalidaExceptionException ex,
            WebRequest request) {

        log.warn("URL inválida: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            "INVALID_URL",
            null,
            System.currentTimeMillis()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Trata UrlExpiredException
     */
    @ExceptionHandler(UrlExpiredException.class)
    public ResponseEntity<ErrorResponse> handleUrlExpiredException(
            UrlExpiredException ex,
            WebRequest request) {

        log.warn("URL expirada: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.GONE.value(),
            ex.getMessage(),
            "URL_EXPIRED",
            null,
            System.currentTimeMillis()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.GONE);
    }

    /**
     * Trata exceções genéricas
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            WebRequest request) {

        log.error("Erro inesperado", ex);

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Erro interno do servidor",
            "INTERNAL_SERVER_ERROR",
            null,
            System.currentTimeMillis()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

