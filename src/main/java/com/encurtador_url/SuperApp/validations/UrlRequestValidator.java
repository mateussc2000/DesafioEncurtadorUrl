package com.encurtador_url.SuperApp.validations;

import com.encurtador_url.SuperApp.exception.UrlInvalidaExceptionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.LocalDateTime;

/**
 * Validador para requests de encurtamento de URLs
 * Validações básicas que não precisam de acesso ao banco
 */
@Component
@Slf4j
public class UrlRequestValidator implements UrlValidator {

    /**
     * Valida a URL original conforme PDF
     * - Não pode ser vazia ou nula
     * - Deve ter protocolo HTTP ou HTTPS
     * - Deve ter host válido
     * - Tamanho máximo
     */
    public void validateOriginalUrl(String originalUrl) {
        log.debug("Validando URL original: {}", originalUrl);

        if (originalUrl == null || originalUrl.isBlank()) {
            throw new UrlInvalidaExceptionException("URL original não pode estar vazia");
        }

        // Remove espaços em branco
        originalUrl = originalUrl.trim();

        // Validações básicas de formato
        if (!originalUrl.startsWith("http://") && !originalUrl.startsWith("https://")) {
            throw new UrlInvalidaExceptionException("URL deve usar protocolo HTTP ou HTTPS");
        }

        // Validação mais rigorosa usando URI (java.net.URL está deprecated desde Java 20)
        try {
            URI uri = URI.create(originalUrl);

            // Verifica se tem scheme (protocolo)
            if (uri.getScheme() == null || uri.getScheme().isEmpty()) {
                throw new UrlInvalidaExceptionException("URL deve conter um protocolo válido");
            }

            // Verifica se tem host
            if (uri.getHost() == null || uri.getHost().isEmpty()) {
                throw new UrlInvalidaExceptionException("URL deve conter um host válido");
            }
        } catch (IllegalArgumentException e) {
            throw new UrlInvalidaExceptionException("URL inválida: " + e.getMessage());
        }

        // Validação de tamanho máximo
        if (originalUrl.length() > 2048) {
            throw new UrlInvalidaExceptionException("URL excede tamanho máximo de 2048 caracteres");
        }

        log.debug("URL original validada com sucesso");
    }

    /**
     * Valida alias customizado conforme PDF
     * - Deve ter entre 3 e 50 caracteres
     * - Apenas letras, números, hífen e underscore
     */
    public void validateCustomAlias(String customAlias) {
        log.debug("Validando alias customizado: {}", customAlias);

        if (customAlias.length() < 3 || customAlias.length() > 50) {
            throw new UrlInvalidaExceptionException("Alias deve ter entre 3 e 50 caracteres");
        }

        if (!customAlias.matches("^[a-zA-Z0-9_-]+$")) {
            throw new UrlInvalidaExceptionException("Alias deve conter apenas letras, números, hífen e underscore");
        }

        log.debug("Alias customizado validado com sucesso");
    }

    /**
     * Valida data de expiração
     * - Não pode ser no passado
     * - Deve ser uma data futura razoável
     */
    public void validateExpirationDate(LocalDateTime expirationDate) {
        log.debug("Validando data de expiração: {}", expirationDate);

        LocalDateTime now = LocalDateTime.now();

        if (expirationDate.isBefore(now)) {
            throw new UrlInvalidaExceptionException("Data de expiração não pode ser no passado");
        }

        // Validação opcional: não pode ser muito distante (máximo 1 ano)
        LocalDateTime maxExpiration = now.plusYears(1);
        if (expirationDate.isAfter(maxExpiration)) {
            throw new UrlInvalidaExceptionException("Data de expiração não pode ser superior a 1 ano");
        }

        log.debug("Data de expiração validada com sucesso");
    }

    /**
     * Validação básica do shortCode
     * - Não pode ser vazio
     * - Deve ter tamanho razoável
     * - Apenas caracteres permitidos
     */
    public void validateShortCode(String shortCode) {
        log.debug("Validando shortCode: {}", shortCode);

        if (shortCode == null || shortCode.isBlank()) {
            throw new UrlInvalidaExceptionException("Código curto não pode estar vazio");
        }

        if (shortCode.length() < 3 || shortCode.length() > 50) {
            throw new UrlInvalidaExceptionException("Código curto deve ter entre 3 e 50 caracteres");
        }

        // Validação de formato (letras, números, hífen, underscore)
        if (!shortCode.matches("^[a-zA-Z0-9_-]+$")) {
            throw new UrlInvalidaExceptionException("Código curto contém caracteres inválidos");
        }

        log.debug("ShortCode validado com sucesso");
    }
}
