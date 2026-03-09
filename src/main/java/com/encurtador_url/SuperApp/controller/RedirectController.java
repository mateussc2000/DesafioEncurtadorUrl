package com.encurtador_url.SuperApp.controller;

import com.encurtador_url.SuperApp.service.ShortenUrlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Controller para redirecionar URLs curtas a partir da raiz
 * Exemplo: GET /abc123 → redireciona para a URL original
 */
@RestController
@Slf4j
public class RedirectController {

    @Autowired
    private ShortenUrlService service;

    /**
     * Redireciona para a URL original baseado no short code
     *
     * @param shortCode código curto da URL
     * @return Redirecionamento para a URL original
     */
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirectShortUrl(@PathVariable String shortCode) {
        // Valida o formato do short code
        if (!isValidShortCode(shortCode)) {
            return ResponseEntity.notFound().build();
        }

        Optional<String> originalUrl = service.redirectToOriginalUrl(shortCode);

        if (originalUrl.isPresent()) {
            log.info("Redirecionando {} para {}", shortCode, originalUrl.get());
            return ResponseEntity.status(HttpStatus.FOUND)
                                .header("Location", originalUrl.get())
                                .build();
        }

        log.warn("Short code não encontrado: {}", shortCode);
        return ResponseEntity.notFound().build();
    }

    /**
     * Valida se o short code tem o formato esperado
     * Evita conflitos com rotas conhecidas
     *
     * @param shortCode código a validar
     * @return true se válido
     */
    private boolean isValidShortCode(String shortCode) {
        // Ignora rotas conhecidas do framework
        if (shortCode.startsWith("api") || shortCode.startsWith("swagger") ||
            shortCode.startsWith("h2-console") || shortCode.equals("actuator")) {
            return false;
        }

        // Valida tamanho e caracteres
        return shortCode.matches("^[0-9A-Za-z]{3,10}$");
    }
}

