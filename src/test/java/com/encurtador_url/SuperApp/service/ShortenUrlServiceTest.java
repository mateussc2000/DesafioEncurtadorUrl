package com.encurtador_url.SuperApp.service;

import com.encurtador_url.SuperApp.dto.request.ShortenUrlRequest;
import com.encurtador_url.SuperApp.dto.response.ShortenUrlResponse;
import com.encurtador_url.SuperApp.model.ShortenedUrl;
import com.encurtador_url.SuperApp.repository.ShortenedUrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração para ShortenUrlService com H2 em memória
 */
@SpringBootTest
@ActiveProfiles("test")
public class ShortenUrlServiceTest {

    @Autowired
    private ShortenUrlService service;

    @Autowired
    private ShortenedUrlRepository repository;

    @BeforeEach
    public void setUp() {
        repository.deleteAll();
    }

    @Test
    public void testShortenUrl() {
        ShortenUrlRequest request = new ShortenUrlRequest("https://www.example.com/very/long/url", null, null);
        ShortenUrlResponse response = service.shortenUrl(request);

        assertNotNull(response);
        assertNotNull(response.shortCode());
        assertEquals("https://www.example.com/very/long/url", response.originalUrl());
        assertEquals(6, response.shortCode().length());
    }

    @Test
    public void testShortenUrlAlreadyExists() {
        String originalUrl = "https://www.example.com/very/long/url";

        // Primeira requisição
        ShortenUrlRequest request = new ShortenUrlRequest(originalUrl, null, null);
        ShortenUrlResponse response1 = service.shortenUrl(request);
        String shortCode1 = response1.shortCode();

        // Segunda requisição com mesma URL
        ShortenUrlResponse response2 = service.shortenUrl(request);
        String shortCode2 = response2.shortCode();

        // Deve retornar o mesmo código
        assertEquals(shortCode1, shortCode2);

        // Deve existir apenas um registro no banco
        assertTrue(repository.findByShortCode(shortCode1).isPresent());
    }

    @Test
    public void testShortenUrlWithEmptyUrl() {
        assertThrows(Exception.class, () -> {
            service.shortenUrl(new ShortenUrlRequest("", null, null));
        });
    }

    @Test
    public void testGetShortenedUrl() {
        ShortenUrlRequest request = new ShortenUrlRequest("https://www.example.com", null, null);
        ShortenUrlResponse created = service.shortenUrl(request);

        Optional<ShortenUrlResponse> retrieved = service.getShortenedUrl(created.shortCode());

        assertTrue(retrieved.isPresent());
        assertEquals(created.originalUrl(), retrieved.get().originalUrl());
    }

    @Test
    public void testRedirectToOriginalUrl() {
        ShortenUrlRequest request = new ShortenUrlRequest("https://www.example.com", null, null);
        ShortenUrlResponse created = service.shortenUrl(request);

        Optional<String> originalUrl = service.redirectToOriginalUrl(created.shortCode());

        assertTrue(originalUrl.isPresent());
        assertEquals("https://www.example.com", originalUrl.get());

        // Verifica que click count foi incrementado
        Optional<ShortenUrlResponse> stats = service.getStats(created.shortCode());
        assertTrue(stats.isPresent());
        assertEquals(1, stats.get().clickCount());
    }

    @Test
    public void testDeleteShortenedUrl() {
        ShortenUrlRequest request = new ShortenUrlRequest("https://www.example.com", null, null);
        ShortenUrlResponse created = service.shortenUrl(request);

        boolean deleted = service.deleteShortenedUrl(created.shortCode());
        assertTrue(deleted);

        Optional<ShortenUrlResponse> retrieved = service.getShortenedUrl(created.shortCode());
        assertFalse(retrieved.isPresent());
    }

    @Test
    public void testGetStats() {
        ShortenUrlRequest request = new ShortenUrlRequest("https://www.example.com", null, null);
        ShortenUrlResponse created = service.shortenUrl(request);

        // Simula alguns acessos
        for (int i = 0; i < 5; i++) {
            service.redirectToOriginalUrl(created.shortCode());
        }

        Optional<ShortenUrlResponse> stats = service.getStats(created.shortCode());

        assertTrue(stats.isPresent());
        assertEquals(5, stats.get().clickCount());
        assertNotNull(stats.get().lastAccessed());
    }
}
