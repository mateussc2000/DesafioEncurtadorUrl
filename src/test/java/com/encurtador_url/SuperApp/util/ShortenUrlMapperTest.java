package com.encurtador_url.SuperApp.util;

import com.encurtador_url.SuperApp.dto.response.DetailsUrlResponse;
import com.encurtador_url.SuperApp.dto.response.ShortenUrlResponse;
import com.encurtador_url.SuperApp.model.ShortenedUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para ShortenUrlMapper
 *
 * Para usar este arquivo:
 * 1. Crie em: src/test/java/com/encurtador_url/SuperApp/util/ShortenUrlMapperTest.java
 * 2. Execute: mvn test -Dtest=ShortenUrlMapperTest
 */
@DisplayName("ShortenUrlMapper Tests")
public class ShortenUrlMapperTest {

    private ShortenUrlMapper mapper;
    private ShortenedUrl shortenedUrl;
    private final String BASE_URL = "http://localhost:8080";

    @BeforeEach
    void setUp() {
        mapper = new ShortenUrlMapper();
        // Injetar baseUrl via reflection (simulando @Value)
        ReflectionTestUtils.setField(mapper, "baseUrl", BASE_URL);

        // Criar entidade de teste
        shortenedUrl = ShortenedUrl.builder()
            .shortCode("abc123")
            .originalUrl("https://www.exemplo.com/pagina/muito/longa")
            .createdAt(LocalDateTime.of(2026, 3, 11, 10, 30, 0))
            .expirationDate(LocalDateTime.of(2026, 4, 11, 10, 30, 0))
            .clickCount(42)
            .customAlias(null)
            .lastAccessed(LocalDateTime.of(2026, 3, 11, 14, 45, 30))
            .build();
    }

    @Test
    @DisplayName("Deve converter ShortenedUrl para ShortenUrlResponse corretamente")
    void testToResponse() {
        // Arrange (já feito no setUp)

        // Act
        ShortenUrlResponse response = mapper.toResponse(shortenedUrl);

        // Assert
        assertNotNull(response, "Response não deve ser nula");
        assertEquals("abc123", response.id(), "ID deve ser o shortCode");
        assertEquals("http://localhost:8080/abc123", response.shortUrl(), "shortUrl deve ser construída corretamente");
        assertEquals("https://www.exemplo.com/pagina/muito/longa", response.originalUrl(), "originalUrl deve ser preservada");
        assertEquals(LocalDateTime.of(2026, 3, 11, 10, 30, 0), response.createdAt(), "createdAt deve ser preservado");
        assertEquals(LocalDateTime.of(2026, 4, 11, 10, 30, 0), response.expirationDate(), "expirationDate deve ser preservado");
    }

    @Test
    @DisplayName("Deve converter ShortenedUrl para DetailsUrlResponse com clickCount")
    void testToDetailsResponse() {
        // Arrange (já feito no setUp)

        // Act
        DetailsUrlResponse response = mapper.toDetailsResponse(shortenedUrl);

        // Assert
        assertNotNull(response, "Response não deve ser nula");
        assertEquals("abc123", response.id(), "ID deve ser o shortCode");
        assertEquals("http://localhost:8080/abc123", response.shortUrl(), "shortUrl deve ser construída corretamente");
        assertEquals("https://www.exemplo.com/pagina/muito/longa", response.originalUrl(), "originalUrl deve ser preservada");
        assertEquals(LocalDateTime.of(2026, 3, 11, 10, 30, 0), response.createdAt(), "createdAt deve ser preservado");
        assertEquals(LocalDateTime.of(2026, 4, 11, 10, 30, 0), response.expirationDate(), "expirationDate deve ser preservado");
        assertEquals(42, response.clickCount(), "clickCount deve ser incluído");
    }

    @Test
    @DisplayName("ShortenUrlResponse não deve conter clickCount")
    void testShortenUrlResponseNoClickCount() {
        // Arrange
        shortenedUrl.setClickCount(999);

        // Act
        ShortenUrlResponse response = mapper.toResponse(shortenedUrl);

        // Assert - Verificar que clickCount não está presente (não há campo para comparar)
        assertNotNull(response);
        // Se tentar acessar response.clickCount(), teria erro de compilação
        // Isso prova que o DTO não tem este campo
    }

    @Test
    @DisplayName("URL encurtada deve ser construída com baseUrl + shortCode")
    void testShortUrlConstruction() {
        // Arrange
        ReflectionTestUtils.setField(mapper, "baseUrl", "https://sho.rt");
        shortenedUrl.setShortCode("xyz789");

        // Act
        ShortenUrlResponse response = mapper.toResponse(shortenedUrl);

        // Assert
        assertEquals("https://sho.rt/xyz789", response.shortUrl(), "shortUrl deve usar baseUrl correto");
    }

    @Test
    @DisplayName("Deve lidar com expirationDate nula")
    void testNullExpirationDate() {
        // Arrange
        shortenedUrl.setExpirationDate(null);

        // Act
        ShortenUrlResponse response = mapper.toResponse(shortenedUrl);

        // Assert
        assertNull(response.expirationDate(), "expirationDate nula deve ser preservada");
    }

    @Test
    @DisplayName("Deve lidar com clickCount zero")
    void testZeroClickCount() {
        // Arrange
        shortenedUrl.setClickCount(0);

        // Act
        DetailsUrlResponse response = mapper.toDetailsResponse(shortenedUrl);

        // Assert
        assertEquals(0, response.clickCount(), "clickCount zero deve ser preservado");
    }

    @Test
    @DisplayName("DetailsUrlResponse deve ter todos os 6 campos obrigatórios")
    void testDetailsUrlResponseAllFields() {
        // Arrange
        DetailsUrlResponse response = mapper.toDetailsResponse(shortenedUrl);

        // Act & Assert - Tentar acessar todos os campos
        assertNotNull(response.id());
        assertNotNull(response.shortUrl());
        assertNotNull(response.originalUrl());
        assertNotNull(response.createdAt());
        assertNotNull(response.expirationDate());
        assertNotNull(response.clickCount());
    }

    @Test
    @DisplayName("ShortenUrlResponse deve ter exatamente 5 campos")
    void testShortenUrlResponseFieldCount() {
        // Arrange
        ShortenUrlResponse response = mapper.toResponse(shortenedUrl);

        // Act & Assert - Tentar acessar os 5 campos
        assertNotNull(response.id());
        assertNotNull(response.shortUrl());
        assertNotNull(response.originalUrl());
        assertNotNull(response.createdAt());
        assertNotNull(response.expirationDate());

        // Se tentar acessar um 6º campo (como clickCount), teria erro de compilação
        // assertTrue(response.clickCount() != null); // ❌ Erro: method not found
    }
}

