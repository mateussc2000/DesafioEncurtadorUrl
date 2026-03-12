package com.encurtador_url.SuperApp.util;

import com.encurtador_url.SuperApp.mapper.ShortenUrlMapper;
import com.encurtador_url.SuperApp.mapper.ShortenUrlMapperImpl;
import com.encurtador_url.SuperApp.dto.response.DetailsUrlResponse;
import com.encurtador_url.SuperApp.dto.response.ShortenUrlResponse;
import com.encurtador_url.SuperApp.model.ShortenedUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para ShortenUrlMapper
 */
@DisplayName("ShortenUrlMapper — Testes Unitários")
class ShortenUrlMapperTest {

    // Instância direta da implementação gerada pelo MapStruct — sem Spring
    private final ShortenUrlMapper mapper = new ShortenUrlMapperImpl();

    private ShortenedUrl entity;

    private static final String SHORT_CODE   = "abc123";
    private static final String SHORT_URL    = "http://localhost:8080/abc123";
    private static final String ORIGINAL_URL = "https://www.exemplo.com";
    private static final LocalDateTime CREATED_AT   = LocalDateTime.of(2026, 3, 11, 10, 30, 0);
    private static final LocalDateTime EXPIRATION   = LocalDateTime.of(2026, 4, 11, 10, 30, 0);
    private static final LocalDateTime LAST_ACCESS  = LocalDateTime.of(2026, 3, 11, 15, 45, 30);
    private static final int CLICK_COUNT = 5;

    @BeforeEach
    void setUp() {
        entity = ShortenedUrl.builder()
                .shortCode(SHORT_CODE)
                .shortUrl(SHORT_URL)
                .originalUrl(ORIGINAL_URL)
                .createdAt(CREATED_AT)
                .expirationDate(EXPIRATION)
                .clickCount(CLICK_COUNT)
                .lastAccessed(LAST_ACCESS)
                .build();
    }

    @Nested
    @DisplayName("toResponse()")
    class ToResponse {

        @Test
        @DisplayName("Deve mapear shortCode para o campo id")
        void deveMapearShortCodeParaId() {
            ShortenUrlResponse result = mapper.toResponse(entity);
            assertEquals(SHORT_CODE, result.id());
        }

        @Test
        @DisplayName("Deve mapear todos os campos corretamente")
        void deveMapearTodosOsCampos() {
            ShortenUrlResponse result = mapper.toResponse(entity);

            assertNotNull(result);
            assertEquals(SHORT_CODE,   result.id());
            assertEquals(SHORT_URL,    result.shortUrl());
            assertEquals(ORIGINAL_URL, result.originalUrl());
            assertEquals(CREATED_AT,   result.createdAt());
            assertEquals(EXPIRATION,   result.expirationDate());
        }
    }

    @Nested
    @DisplayName("toDetailsResponse()")
    class ToDetailsResponse {

        @Test
        @DisplayName("Deve mapear shortCode para o campo id")
        void deveMapearShortCodeParaId() {
            DetailsUrlResponse result = mapper.toDetailsResponse(entity);
            assertEquals(SHORT_CODE, result.id());
        }

        @Test
        @DisplayName("Deve mapear todos os campos incluindo clickCount")
        void deveMapearTodosOsCamposComClickCount() {
            DetailsUrlResponse result = mapper.toDetailsResponse(entity);

            assertNotNull(result);
            assertEquals(SHORT_CODE,   result.id());
            assertEquals(SHORT_URL,    result.shortUrl());
            assertEquals(ORIGINAL_URL, result.originalUrl());
            assertEquals(CREATED_AT,   result.createdAt());
            assertEquals(EXPIRATION,   result.expirationDate());
            assertEquals(CLICK_COUNT,  result.clickCount());
        }

        @Test
        @DisplayName("Deve mapear clickCount zero corretamente")
        void deveMapearClickCountZero() {
            ShortenedUrl semCliques = ShortenedUrl.builder()
                    .shortCode(SHORT_CODE).shortUrl(SHORT_URL)
                    .originalUrl(ORIGINAL_URL).createdAt(CREATED_AT)
                    .expirationDate(EXPIRATION).clickCount(0).build();

            DetailsUrlResponse result = mapper.toDetailsResponse(semCliques);
            assertEquals(0, result.clickCount());
        }
    }
}
