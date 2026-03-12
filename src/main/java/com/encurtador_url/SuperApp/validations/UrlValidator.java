package com.encurtador_url.SuperApp.validations;

/**
 * Interface para validadores de URL
 */
public interface UrlValidator {

    /**
     * Valida URL original
     */
    void validateOriginalUrl(String originalUrl);

    /**
     * Valida alias customizado
     */
    void validateCustomAlias(String customAlias);

    /**
     * Valida data de expiração
     */
    void validateExpirationDate(java.time.LocalDateTime expirationDate);

    /**
     * Valida short code
     */
    void validateShortCode(String shortCode);
}
