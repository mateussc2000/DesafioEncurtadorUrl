package com.encurtador_url.SuperApp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO para resposta de erro consistente
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    int status,
    String message,
    String error,
    String path,
    Long timestamp
) {}

