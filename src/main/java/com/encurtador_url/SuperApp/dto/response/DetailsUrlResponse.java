package com.encurtador_url.SuperApp.dto.response;

import java.time.LocalDateTime;

/**
 * DTO para resposta detalhada de URL encurtada
 * Contém campos essenciais + estatísticas de cliques
 * Campos: id, shortUrl, originalUrl, createdAt, expirationDate, clickCount
 */
public record DetailsUrlResponse(
    String id,
    String shortUrl,
    String originalUrl,
    LocalDateTime createdAt,
    LocalDateTime expirationDate,
    Integer clickCount
) {}
