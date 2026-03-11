package com.encurtador_url.SuperApp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

/**
 * DTO para resposta de URL encurtada
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ShortenUrlResponse(
    String shortCode,
    String customAlias,
    String originalUrl,
    String shortUrl,
    LocalDateTime createdAt,
    LocalDateTime expirationDate,
    Integer clickCount,
    LocalDateTime lastAccessed
) {}

