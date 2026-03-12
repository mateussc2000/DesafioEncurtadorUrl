package com.encurtador_url.SuperApp.controller;

import com.encurtador_url.SuperApp.service.ShortenUrlService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Optional;

/**
 * Controller para redirecionar URLs curtas a partir da raiz.
 * Exemplo: GET /abc123 → redireciona para a URL original.
 *
 * Ocultado do Swagger UI com @Hidden para evitar o erro "Failed to fetch":
 * o Swagger usa fetch() que segue o 302 cross-origin automaticamente,
 * e a URL de destino nunca terá cabeçalhos CORS.
 * Use GET /v1/urls/{shortCode} no Swagger para consultar informações da URL.
 */
@RestController
@Slf4j
@Hidden
@CrossOrigin(origins = "*")
public class RedirectController {

    @Autowired
    private ShortenUrlService service;

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirectShortUrl(@PathVariable String shortCode) {
        // Valida o formato do short code
        if (!isValidShortCode(shortCode)) {
            return ResponseEntity.notFound().build();
        }

        Optional<String> originalUrl = service.redirectToOriginalUrl(shortCode);

        if (originalUrl.isPresent()) {
            String url = sanitizeUrl(originalUrl.get());
            log.info("Redirecionando {} → {}", shortCode, url);

            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(url));
            headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }

        log.warn("Short code não encontrado: {}", shortCode);
        return ResponseEntity.notFound().build();
    }

    /**
     * Garante que a URL possui scheme http/https.
     * Evita o erro "URL scheme must be http or https" no browser.
     */
    private String sanitizeUrl(String url) {
        if (url == null || url.isBlank()) return url;
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "https://" + url;
        }
        return url;
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
