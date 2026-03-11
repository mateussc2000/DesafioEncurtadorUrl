package com.encurtador_url.SuperApp.dto.response;

import java.time.LocalDateTime;

/**
 * DTO para resposta de URL encurtada
 * Contém apenas os campos essenciais: id, shortUrl, originalUrl, createdAt, expirationDate
 */
public record ShortenUrlResponse(
    String id,
    String shortUrl,
    String originalUrl,
    LocalDateTime createdAt,
    LocalDateTime expirationDate
) {}
