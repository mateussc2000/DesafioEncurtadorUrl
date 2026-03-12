package com.encurtador_url.SuperApp.util;

import com.encurtador_url.SuperApp.exception.UrlInvalidaExceptionException;
import java.net.URL;

/**
 * Utilitário para validar URLs
 */
public class UrlValidator {

    private static final int MAX_URL_LENGTH = 2048;

    /**
     * Valida se uma URL é bem formada e tem protocolo válido
     * @param url URL a validar
     * @throws UrlInvalidaExceptionException se URL for inválida
     */
    public static void validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new UrlInvalidaExceptionException("URL não pode estar vazia");
        }

        if (url.length() > MAX_URL_LENGTH) {
            throw new UrlInvalidaExceptionException("URL excede tamanho máximo de " + MAX_URL_LENGTH + " caracteres");
        }

        try {
            URL urlObj = new URL(url);
            String protocol = urlObj.getProtocol();

            // Verifica protocolo
            if (!protocol.equals("http") && !protocol.equals("https")) {
                throw new UrlInvalidaExceptionException("URL deve usar protocolo HTTP ou HTTPS");
            }

            // Verifica se tem host
            if (urlObj.getHost() == null || urlObj.getHost().isEmpty()) {
                throw new UrlInvalidaExceptionException("URL deve conter um host válido");
            }
        } catch (Exception e) {
            if (e instanceof UrlInvalidaExceptionException) {
                throw (UrlInvalidaExceptionException) e;
            }
            throw new UrlInvalidaExceptionException("URL inválida: " + e.getMessage(), e);
        }
    }

    /**
     * Valida um alias customizado
     * @param alias alias a validar
     * @throws UrlInvalidaExceptionException se alias for inválido
     */
    public static void validateAlias(String alias) {
        if (alias == null || alias.isBlank()) {
            return; // alias é opcional
        }

        if (alias.length() < 3 || alias.length() > 50) {
            throw new UrlInvalidaExceptionException("Alias deve ter entre 3 e 50 caracteres");
        }

        if (!alias.matches("^[a-zA-Z0-9_-]+$")) {
            throw new UrlInvalidaExceptionException("Alias deve conter apenas letras, números, hífen e underscore");
        }
    }
}

