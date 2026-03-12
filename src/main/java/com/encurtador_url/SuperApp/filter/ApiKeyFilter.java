package com.encurtador_url.SuperApp.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que exige o header X-API-Key para operações protegidas.
 * Rotas protegidas: POST /v1/urls/, DELETE /v1/urls/{shortCode}
 * Demais rotas (GET, redirect, Swagger, H2) são públicas.
 */
@Component
@Slf4j
public class ApiKeyFilter extends OncePerRequestFilter {

    public static final String API_KEY_HEADER = "X-API-Key";

    @Value("${app.api-key}")
    private String configuredApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (!requiresApiKey(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String providedKey = request.getHeader(API_KEY_HEADER);

        if (providedKey == null || providedKey.isBlank()) {
            log.warn("Requisição sem X-API-Key: {} {}", request.getMethod(), request.getRequestURI());
            writeUnauthorized(response, "Header X-API-Key é obrigatório");
            return;
        }

        if (!configuredApiKey.equals(providedKey)) {
            log.warn("X-API-Key inválida recebida: {} {}", request.getMethod(), request.getRequestURI());
            writeUnauthorized(response, "X-API-Key inválida");
            return;
        }

        log.debug("X-API-Key válida: {} {}", request.getMethod(), request.getRequestURI());
        filterChain.doFilter(request, response);
    }

    /**
     * Define quais rotas exigem autenticação por API Key.
     */
    private boolean requiresApiKey(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();

        // POST /v1/urls/ — criar URL
        if (HttpMethod.POST.matches(method) && uri.startsWith("/v1/urls")) {
            return true;
        }

        // DELETE /v1/urls/{shortCode} — deletar URL
        if (HttpMethod.DELETE.matches(method) && uri.startsWith("/v1/urls")) {
            return true;
        }

        return false;
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("""
                {
                  "status": 401,
                  "error": "UNAUTHORIZED",
                  "message": "%s",
                  "timestamp": %d
                }
                """.formatted(message, System.currentTimeMillis()));
    }
}

