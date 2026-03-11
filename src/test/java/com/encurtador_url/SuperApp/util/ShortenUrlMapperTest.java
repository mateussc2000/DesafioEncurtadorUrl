package com.encurtador_url.SuperApp.util;

import com.encurtador_url.SuperApp.mapper.ShortenUrlMapper;
import com.encurtador_url.SuperApp.dto.response.DetailsUrlResponse;
import com.encurtador_url.SuperApp.dto.response.ShortenUrlResponse;
import com.encurtador_url.SuperApp.model.ShortenedUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para ShortenUrlMapper
 */
@SpringBootTest
@DisplayName("ShortenUrlMapper Tests")
public class ShortenUrlMapperTest {

    @Autowired
    private ShortenUrlMapper mapper;

    private ShortenedUrl shortenedUrl;
    private final String SHORT_CODE = "abc123";

    @BeforeEach
    void setUp() {
        shortenedUrl = ShortenedUrl.builder()
            .shortCode(SHORT_CODE)
            .originalUrl("https://www.exemplo.com")
            .createdAt(LocalDateTime.of(2026, 3, 11, 10, 30, 0))
            .expirationDate(LocalDateTime.of(2026, 4, 11, 10, 30, 0))
            .clickCount(5)
            .lastAccessed(LocalDateTime.of(2026, 3, 11, 15, 45, 30))
            .build();
    }

    @Test
    @DisplayName("Deve mapear para ShortenUrlResponse")
    void testToResponse() {
        ShortenUrlResponse response = mapper.toResponse(shortenedUrl);
        assertNotNull(response);
        assertEquals(SHORT_CODE, response.id());
    }

    @Test
    @DisplayName("Deve mapear para DetailsUrlResponse")
    void testToDetailsResponse() {
        DetailsUrlResponse response = mapper.toDetailsResponse(shortenedUrl);
        assertNotNull(response);
        assertEquals(SHORT_CODE, response.id());
    }
}

