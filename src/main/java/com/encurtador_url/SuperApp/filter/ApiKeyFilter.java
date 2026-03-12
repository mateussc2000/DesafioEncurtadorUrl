package com.encurtador_url.SuperApp.filter;

import com.encurtador_url.SuperApp.dto.response.ErrorResponse;
import com.encurtador_url.SuperApp.enums.ErrorCodeEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {

    public static final String API_KEY_HEADER = "X-API-Key";

    @Value("${app.api-key}")
    private String configuredApiKey;

    private final ObjectMapper objectMapper;

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
            writeErrorResponse(response, ErrorCodeEnum.ERRO_API_KEY_AUSENTE, request.getRequestURI());
            return;
        }

        if (!configuredApiKey.equals(providedKey)) {
            log.warn("X-API-Key inválida recebida: {} {}", request.getMethod(), request.getRequestURI());
            writeErrorResponse(response, ErrorCodeEnum.ERRO_API_KEY_INVALIDA, request.getRequestURI());
            return;
        }

        log.debug("X-API-Key válida: {} {}", request.getMethod(), request.getRequestURI());
        filterChain.doFilter(request, response);
    }

    private boolean requiresApiKey(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();

        if (HttpMethod.POST.matches(method) && uri.startsWith("/v1/urls")) {
            return true;
        }

        if (HttpMethod.DELETE.matches(method) && uri.startsWith("/v1/urls")) {
            return true;
        }

        return false;
    }

    private void writeErrorResponse(HttpServletResponse response,
                                    ErrorCodeEnum errorCodeEnum,
                                    String path) throws IOException {
        ErrorResponse<?> body = new ErrorResponse<>(errorCodeEnum, HttpStatus.UNAUTHORIZED.value(), path);

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }
}

