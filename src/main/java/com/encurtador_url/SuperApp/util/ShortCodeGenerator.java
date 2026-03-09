package com.encurtador_url.SuperApp.util;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

/**
 * Utilitário para gerar identificadores curtos de URLs
 * Usa algoritmo Base62 para gerar códigos compactos e URL-safe
 */
public class ShortCodeGenerator {

    private static final String CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    /**
     * Gera um identificador curto aleatório
     * Exemplo: abc123, XyZ789
     *
     * @return String com 6 caracteres alfanuméricos
     */
    public static String generateRandomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARSET.charAt(random.nextInt(CHARSET.length())));
        }
        return sb.toString();
    }

    /**
     * Gera um identificador curto baseado em hash da URL original
     * Menos colisões que aleatório, mas determinístico
     *
     * @param input String para gerar hash
     * @return String com até 8 caracteres
     */
    public static String generateHashCode(String input) {
        long hash = Math.abs(input.hashCode());
        StringBuilder sb = new StringBuilder();

        while (hash > 0 && sb.length() < 8) {
            sb.append(CHARSET.charAt((int) (hash % 62)));
            hash /= 62;
        }

        // Garante mínimo de 6 caracteres
        while (sb.length() < 6) {
            sb.append(CHARSET.charAt(random.nextInt(CHARSET.length())));
        }

        return sb.toString();
    }

    /**
     * Converte um número para Base62
     * Útil para IDs sequenciais
     * Exemplo: 1 → "1", 62 → "10", 3844 → "100"
     *
     * @param num número a converter
     * @return String em base62
     */
    public static String encodeBase62(long num) {
        if (num == 0) return "0";

        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            sb.append(CHARSET.charAt((int) (num % 62)));
            num /= 62;
        }
        return sb.reverse().toString();
    }

    /**
     * Decodifica uma string Base62 para número
     *
     * @param encoded string em base62
     * @return número decodificado
     */
    public static long decodeBase62(String encoded) {
        long num = 0;
        for (char c : encoded.toCharArray()) {
            num = num * 62 + CHARSET.indexOf(c);
        }
        return num;
    }

    /**
     * Valida se um short code tem o formato correto
     *
     * @param shortCode código a validar
     * @return true se válido, false caso contrário
     */
    public static boolean isValidShortCode(String shortCode) {
        if (shortCode == null || shortCode.length() < 3 || shortCode.length() > 10) {
            return false;
        }
        return shortCode.matches("^[0-9A-Za-z]+$");
    }

    /**
     * Gera múltiplos códigos únicos
     * Evita colisões gerando um conjunto de códigos diferentes
     *
     * @param count quantidade de códigos
     * @return Set com códigos únicos
     */
    public static Set<String> generateUniqueCodes(int count) {
        Set<String> codes = new HashSet<>();
        int attempts = 0;
        int maxAttempts = count * 10;

        while (codes.size() < count && attempts < maxAttempts) {
            codes.add(generateRandomCode());
            attempts++;
        }

        return codes;
    }
}

