package com.encurtador_url.SuperApp.validations;

import com.encurtador_url.SuperApp.exception.UrlInvalidaExceptionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UrlRequestValidator — Testes Unitários")
class UrlRequestValidatorTest {

    // Instância direta — sem Spring, sem contexto
    private final UrlRequestValidator validator = new UrlRequestValidator();

    // -------------------------------------------------------------------------
    // validateOriginalUrl
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("validateOriginalUrl()")
    class ValidateOriginalUrl {

        @ParameterizedTest
        @ValueSource(strings = {
                "https://www.example.com",
                "http://example.com",
                "https://sub.domain.com/path?q=1&r=2",
                "https://example.com/path/with/slashes"
        })
        @DisplayName("Deve aceitar URLs válidas com HTTP/HTTPS")
        void deveAceitarUrlsValidas(String url) {
            assertDoesNotThrow(() -> validator.validateOriginalUrl(url));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("Deve lançar exceção para URL nula, vazia ou em branco")
        void deveLancarExcecaoParaUrlNulaOuVazia(String url) {
            assertThrows(UrlInvalidaExceptionException.class,
                    () -> validator.validateOriginalUrl(url));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "ftp://example.com",
                "www.example.com",
                "example.com",
                "//example.com"
        })
        @DisplayName("Deve lançar exceção para URL sem protocolo HTTP/HTTPS")
        void deveLancarExcecaoParaUrlSemProtocolo(String url) {
            UrlInvalidaExceptionException ex = assertThrows(UrlInvalidaExceptionException.class,
                    () -> validator.validateOriginalUrl(url));
            assertTrue(ex.getMessage().contains("HTTP") || ex.getMessage().contains("HTTPS") || ex.getMessage().contains("protocolo"));
        }

        @Test
        @DisplayName("Deve lançar exceção para URL com mais de 2048 caracteres")
        void deveLancarExcecaoParaUrlMuitoLonga() {
            String urlLonga = "https://www.example.com/" + "a".repeat(2100);
            assertThrows(UrlInvalidaExceptionException.class,
                    () -> validator.validateOriginalUrl(urlLonga));
        }

        @Test
        @DisplayName("Deve aceitar URL com exatamente 2048 caracteres")
        void deveAceitarUrlComExatamente2048Caracteres() {
            // "https://www.example.com/" = 24 chars, falta 2024
            String url = "https://www.example.com/" + "a".repeat(2024);
            assertEquals(2048, url.length());
            assertDoesNotThrow(() -> validator.validateOriginalUrl(url));
        }
    }

    // -------------------------------------------------------------------------
    // validateCustomAlias
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("validateCustomAlias()")
    class ValidateCustomAlias {

        @ParameterizedTest
        @ValueSource(strings = {"meu-alias", "alias_123", "ABC", "abc123", "valid-alias"})
        @DisplayName("Deve aceitar aliases válidos")
        void deveAceitarAliasesValidos(String alias) {
            assertDoesNotThrow(() -> validator.validateCustomAlias(alias));
        }

        @Test
        @DisplayName("Deve lançar exceção para alias com menos de 3 caracteres")
        void deveLancarExcecaoParaAliasCurto() {
            UrlInvalidaExceptionException ex = assertThrows(UrlInvalidaExceptionException.class,
                    () -> validator.validateCustomAlias("ab"));
            assertTrue(ex.getMessage().contains("3"));
        }

        @Test
        @DisplayName("Deve lançar exceção para alias com mais de 50 caracteres")
        void deveLancarExcecaoParaAliasLongo() {
            String aliasLongo = "a".repeat(51);
            assertThrows(UrlInvalidaExceptionException.class,
                    () -> validator.validateCustomAlias(aliasLongo));
        }

        @ParameterizedTest
        @ValueSource(strings = {"alias com espaço", "alias.ponto", "alias/barra", "alias@arroba"})
        @DisplayName("Deve lançar exceção para alias com caracteres inválidos")
        void deveLancarExcecaoParaAliasComCaracteresInvalidos(String alias) {
            assertThrows(UrlInvalidaExceptionException.class,
                    () -> validator.validateCustomAlias(alias));
        }
    }

    // -------------------------------------------------------------------------
    // validateExpirationDate
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("validateExpirationDate()")
    class ValidateExpirationDate {

        @Test
        @DisplayName("Deve aceitar data futura válida")
        void deveAceitarDataFuturaValida() {
            assertDoesNotThrow(() ->
                    validator.validateExpirationDate(LocalDateTime.now().plusMonths(6)));
        }

        @Test
        @DisplayName("Deve lançar exceção para data no passado")
        void deveLancarExcecaoParaDataNoPassado() {
            UrlInvalidaExceptionException ex = assertThrows(UrlInvalidaExceptionException.class,
                    () -> validator.validateExpirationDate(LocalDateTime.now().minusDays(1)));
            assertTrue(ex.getMessage().contains("passado"));
        }

        @Test
        @DisplayName("Deve lançar exceção para data superior a 1 ano")
        void deveLancarExcecaoParaDataSuperiorA1Ano() {
            UrlInvalidaExceptionException ex = assertThrows(UrlInvalidaExceptionException.class,
                    () -> validator.validateExpirationDate(LocalDateTime.now().plusYears(1).plusDays(1)));
            assertTrue(ex.getMessage().contains("1 ano"));
        }

        @Test
        @DisplayName("Deve aceitar data exatamente em 1 ano (limite)")
        void deveAceitarDataExatamenteEm1Ano() {
            // Subtrai 1 segundo para garantir que fica dentro do limite
            assertDoesNotThrow(() ->
                    validator.validateExpirationDate(LocalDateTime.now().plusYears(1).minusSeconds(1)));
        }
    }

    // -------------------------------------------------------------------------
    // validateShortCode
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("validateShortCode()")
    class ValidateShortCode {

        @ParameterizedTest
        @ValueSource(strings = {"abc123", "XYZ", "123456", "valid-code", "valid_code"})
        @DisplayName("Deve aceitar short codes válidos")
        void deveAceitarShortCodesValidos(String code) {
            assertDoesNotThrow(() -> validator.validateShortCode(code));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("Deve lançar exceção para short code nulo ou vazio")
        void deveLancarExcecaoParaShortCodeNuloOuVazio(String code) {
            assertThrows(UrlInvalidaExceptionException.class,
                    () -> validator.validateShortCode(code));
        }

        @Test
        @DisplayName("Deve lançar exceção para short code muito curto")
        void deveLancarExcecaoParaShortCodeCurto() {
            assertThrows(UrlInvalidaExceptionException.class,
                    () -> validator.validateShortCode("ab"));
        }

        @Test
        @DisplayName("Deve lançar exceção para short code muito longo")
        void deveLancarExcecaoParaShortCodeLongo() {
            assertThrows(UrlInvalidaExceptionException.class,
                    () -> validator.validateShortCode("a".repeat(51)));
        }

        @ParameterizedTest
        @ValueSource(strings = {"code with space", "code.dot", "code@at"})
        @DisplayName("Deve lançar exceção para short code com caracteres inválidos")
        void deveLancarExcecaoParaShortCodeComCaracteresInvalidos(String code) {
            assertThrows(UrlInvalidaExceptionException.class,
                    () -> validator.validateShortCode(code));
        }
    }
}

