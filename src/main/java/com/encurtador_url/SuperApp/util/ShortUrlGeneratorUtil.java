package com.encurtador_url.SuperApp.util;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utilitário para gerar URLs encurtadas com basepath configurável
 */
@Component
@Getter
public class ShortUrlGeneratorUtil {

    @Value("${app.shortener.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Gera a URL encurtada completa com o basepath configurado
     *
     * @param shortCode código curto gerado
     * @return URL encurtada completa (ex: http://localhost:8080/abc123)
     */
    public String generateShortUrl(String shortCode) {
        return baseUrl + "/" + shortCode;
    }
}

