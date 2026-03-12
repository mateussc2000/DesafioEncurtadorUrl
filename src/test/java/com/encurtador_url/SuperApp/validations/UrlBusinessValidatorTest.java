package com.encurtador_url.SuperApp.validations;

import com.encurtador_url.SuperApp.exception.UrlInvalidaExceptionException;
import com.encurtador_url.SuperApp.model.ShortenedUrl;
import com.encurtador_url.SuperApp.repository.ShortenedUrlRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UrlBusinessValidator — Testes Unitários")
class UrlBusinessValidatorTest {

    @Mock
    private ShortenedUrlRepository repository;

    @InjectMocks
    private UrlBusinessValidator validator;

    // -------------------------------------------------------------------------
    // validateCustomAliasUniqueness
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("validateCustomAliasUniqueness()")
    class ValidateCustomAliasUniqueness {

        @Test
        @DisplayName("Deve aceitar alias que não existe no banco")
        void deveAceitarAliasInexistente() {
            when(repository.existsByCustomAlias("novo-alias")).thenReturn(false);

            assertDoesNotThrow(() -> validator.validateCustomAliasUniqueness("novo-alias"));
            verify(repository).existsByCustomAlias("novo-alias");
        }

        @Test
        @DisplayName("Deve lançar exceção para alias já existente")
        void deveLancarExcecaoParaAliasExistente() {
            when(repository.existsByCustomAlias("existente")).thenReturn(true);

            UrlInvalidaExceptionException ex = assertThrows(UrlInvalidaExceptionException.class,
                    () -> validator.validateCustomAliasUniqueness("existente"));

            assertTrue(ex.getMessage().contains("existente"));
        }

        @Test
        @DisplayName("Deve ignorar alias nulo sem consultar o banco")
        void deveIgnorarAliasNulo() {
            assertDoesNotThrow(() -> validator.validateCustomAliasUniqueness(null));
            verifyNoInteractions(repository);
        }

        @Test
        @DisplayName("Deve ignorar alias em branco sem consultar o banco")
        void deveIgnorarAliasEmBranco() {
            assertDoesNotThrow(() -> validator.validateCustomAliasUniqueness("   "));
            verifyNoInteractions(repository);
        }

        @Test
        @DisplayName("Deve trimar o alias antes de consultar o banco")
        void deveTrimarAliasAntesDeConsultar() {
            when(repository.existsByCustomAlias("alias")).thenReturn(false);

            assertDoesNotThrow(() -> validator.validateCustomAliasUniqueness("  alias  "));
            verify(repository).existsByCustomAlias("alias");
        }
    }

    // -------------------------------------------------------------------------
    // checkExistingUrl
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("checkExistingUrl()")
    class CheckExistingUrl {

        @Test
        @DisplayName("Deve retornar null quando URL não existe")
        void deveRetornarNullQuandoUrlNaoExiste() {
            when(repository.findByOriginalUrl("https://www.new.com")).thenReturn(Optional.empty());

            String result = validator.checkExistingUrl("https://www.new.com");

            assertNull(result);
        }

        @Test
        @DisplayName("Deve retornar o short code quando URL já existe")
        void deveRetornarShortCodeQuandoUrlExiste() {
            ShortenedUrl existing = ShortenedUrl.builder()
                    .shortCode("abc123")
                    .originalUrl("https://www.existing.com")
                    .build();
            when(repository.findByOriginalUrl("https://www.existing.com"))
                    .thenReturn(Optional.of(existing));

            String result = validator.checkExistingUrl("https://www.existing.com");

            assertEquals("abc123", result);
        }

        @Test
        @DisplayName("Deve trimar a URL antes de consultar o banco")
        void deveTrimarUrlAntesDeConsultar() {
            when(repository.findByOriginalUrl("https://www.example.com")).thenReturn(Optional.empty());

            validator.checkExistingUrl("  https://www.example.com  ");

            verify(repository).findByOriginalUrl("https://www.example.com");
        }
    }
}

