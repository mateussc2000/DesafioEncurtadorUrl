package com.encurtador_url.SuperApp.service;

import com.encurtador_url.SuperApp.dto.request.ShortenUrlRequest;
import com.encurtador_url.SuperApp.dto.response.DetailsUrlResponse;
import com.encurtador_url.SuperApp.dto.response.ShortenUrlListResponse;
import com.encurtador_url.SuperApp.dto.response.ShortenUrlResponse;
import com.encurtador_url.SuperApp.exception.UrlExpiredException;
import com.encurtador_url.SuperApp.exception.UrlInvalidaExceptionException;
import com.encurtador_url.SuperApp.exception.UrlNotFoundException;
import com.encurtador_url.SuperApp.mapper.ShortenUrlMapper;
import com.encurtador_url.SuperApp.model.ShortenedUrl;
import com.encurtador_url.SuperApp.repository.ShortenedUrlRepository;
import com.encurtador_url.SuperApp.validations.UrlBusinessValidator;
import com.encurtador_url.SuperApp.validations.UrlRequestValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShortenUrlServiceImpl — Testes Unitários")
class ShortenUrlServiceTest {

    @Mock
    private ShortenedUrlRepository repository;

    @Mock
    private UrlRequestValidator requestValidator;

    @Mock
    private UrlBusinessValidator businessValidator;

    @Mock
    private ShortenUrlMapper mapper;

    @InjectMocks
    private ShortenUrlServiceImpl service;

    private static final String SHORT_CODE   = "abc123";
    private static final String ORIGINAL_URL = "https://www.example.com/very/long/url";
    private static final String SHORT_URL    = "http://localhost:8080/abc123";

    private ShortenedUrl entity;
    private ShortenUrlResponse response;
    private DetailsUrlResponse detailsResponse;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "baseUrl", "http://localhost:8080");

        entity = ShortenedUrl.builder()
                .shortCode(SHORT_CODE)
                .shortUrl(SHORT_URL)
                .originalUrl(ORIGINAL_URL)
                .createdAt(LocalDateTime.now())
                .expirationDate(LocalDateTime.now().plusYears(1))
                .clickCount(0)
                .build();

        response = new ShortenUrlResponse(SHORT_CODE, SHORT_URL, ORIGINAL_URL,
                entity.getCreatedAt(), entity.getExpirationDate());

        detailsResponse = new DetailsUrlResponse(SHORT_CODE, SHORT_URL, ORIGINAL_URL,
                entity.getCreatedAt(), entity.getExpirationDate(), 0);
    }

    // -------------------------------------------------------------------------
    // shortenUrl
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("shortenUrl()")
    class ShortenUrl {

        @Test
        @DisplayName("Deve encurtar uma URL nova com sucesso")
        void deveEncurtarUrlNova() {
            doNothing().when(requestValidator).validateOriginalUrl(ORIGINAL_URL);
            when(businessValidator.checkExistingUrl(ORIGINAL_URL)).thenReturn(null);
            when(repository.existsByShortCode(anyString())).thenReturn(false);
            when(repository.save(any(ShortenedUrl.class))).thenReturn(entity);
            when(mapper.toResponse(entity)).thenReturn(response);

            ShortenUrlResponse result = service.shortenUrl(new ShortenUrlRequest(ORIGINAL_URL, null, null));

            assertNotNull(result);
            assertEquals(SHORT_CODE, result.id());
            assertEquals(ORIGINAL_URL, result.originalUrl());
            verify(repository).save(any(ShortenedUrl.class));
        }

        @Test
        @DisplayName("Deve retornar URL existente sem criar nova")
        void deveRetornarUrlExistente() {
            doNothing().when(requestValidator).validateOriginalUrl(ORIGINAL_URL);
            when(businessValidator.checkExistingUrl(ORIGINAL_URL)).thenReturn(SHORT_CODE);
            when(repository.findByShortCode(SHORT_CODE)).thenReturn(Optional.of(entity));
            when(mapper.toResponse(entity)).thenReturn(response);

            ShortenUrlResponse result = service.shortenUrl(new ShortenUrlRequest(ORIGINAL_URL, null, null));

            assertNotNull(result);
            assertEquals(SHORT_CODE, result.id());
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Deve encurtar com alias customizado válido")
        void deveEncurtarComAliasCustomizado() {
            String alias = "meu-alias";
            ShortenedUrl entityAlias = ShortenedUrl.builder()
                    .shortCode(alias).shortUrl("http://localhost:8080/" + alias)
                    .originalUrl(ORIGINAL_URL).createdAt(LocalDateTime.now())
                    .expirationDate(LocalDateTime.now().plusYears(1)).clickCount(0).build();
            ShortenUrlResponse aliasResponse = new ShortenUrlResponse(alias,
                    "http://localhost:8080/" + alias, ORIGINAL_URL,
                    entityAlias.getCreatedAt(), entityAlias.getExpirationDate());

            doNothing().when(requestValidator).validateOriginalUrl(ORIGINAL_URL);
            doNothing().when(requestValidator).validateCustomAlias(alias);
            when(businessValidator.checkExistingUrl(ORIGINAL_URL)).thenReturn(null);
            doNothing().when(businessValidator).validateCustomAliasUniqueness(alias);
            when(repository.save(any(ShortenedUrl.class))).thenReturn(entityAlias);
            when(mapper.toResponse(entityAlias)).thenReturn(aliasResponse);

            ShortenUrlResponse result = service.shortenUrl(new ShortenUrlRequest(ORIGINAL_URL, alias, null));

            assertEquals(alias, result.id());
        }

        @Test
        @DisplayName("Deve lançar UrlInvalidaExceptionException para URL inválida")
        void deveLancarExcecaoParaUrlInvalida() {
            doThrow(new UrlInvalidaExceptionException("URL deve usar protocolo HTTP ou HTTPS"))
                    .when(requestValidator).validateOriginalUrl("ftp://invalida.com");

            assertThrows(UrlInvalidaExceptionException.class,
                    () -> service.shortenUrl(new ShortenUrlRequest("ftp://invalida.com", null, null)));

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar UrlInvalidaExceptionException para alias já existente")
        void deveLancarExcecaoParaAliasDuplicado() {
            String alias = "existente";
            doNothing().when(requestValidator).validateOriginalUrl(ORIGINAL_URL);
            doNothing().when(requestValidator).validateCustomAlias(alias);
            when(businessValidator.checkExistingUrl(ORIGINAL_URL)).thenReturn(null);
            doThrow(new UrlInvalidaExceptionException("Alias customizado 'existente' já existe"))
                    .when(businessValidator).validateCustomAliasUniqueness(alias);

            assertThrows(UrlInvalidaExceptionException.class,
                    () -> service.shortenUrl(new ShortenUrlRequest(ORIGINAL_URL, alias, null)));
        }
    }

    // -------------------------------------------------------------------------
    // getShortenedUrl
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("getShortenedUrl()")
    class GetShortenedUrl {

        @Test
        @DisplayName("Deve retornar URL encurtada quando encontrada")
        void deveRetornarUrlEncontrada() {
            when(repository.findByShortCode(SHORT_CODE)).thenReturn(Optional.of(entity));
            when(mapper.toResponse(entity)).thenReturn(response);

            Optional<ShortenUrlResponse> result = service.getShortenedUrl(SHORT_CODE);

            assertTrue(result.isPresent());
            assertEquals(ORIGINAL_URL, result.get().originalUrl());
        }

        @Test
        @DisplayName("Deve lançar UrlNotFoundException quando não encontrada")
        void deveLancarExcecaoQuandoNaoEncontrada() {
            when(repository.findByShortCode("inexistente")).thenReturn(Optional.empty());

            assertThrows(UrlNotFoundException.class,
                    () -> service.getShortenedUrl("inexistente"));
        }
    }

    // -------------------------------------------------------------------------
    // redirectToOriginalUrl
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("redirectToOriginalUrl()")
    class RedirectToOriginalUrl {

        @Test
        @DisplayName("Deve redirecionar e incrementar click count")
        void deveRedirecionarEIncrementarClicks() {
            when(repository.findByShortCode(SHORT_CODE)).thenReturn(Optional.of(entity));
            when(repository.save(any(ShortenedUrl.class))).thenReturn(entity);

            Optional<String> result = service.redirectToOriginalUrl(SHORT_CODE);

            assertTrue(result.isPresent());
            assertEquals(ORIGINAL_URL, result.get());
            assertEquals(1, entity.getClickCount());
            verify(repository).save(entity);
        }

        @Test
        @DisplayName("Deve lançar UrlNotFoundException para short code inexistente")
        void deveLancarExcecaoParaShortCodeInexistente() {
            when(repository.findByShortCode("naoexiste")).thenReturn(Optional.empty());

            assertThrows(UrlNotFoundException.class,
                    () -> service.redirectToOriginalUrl("naoexiste"));
        }

        @Test
        @DisplayName("Deve lançar UrlExpiredException para URL expirada")
        void deveLancarExcecaoParaUrlExpirada() {
            ShortenedUrl expirada = ShortenedUrl.builder()
                    .shortCode(SHORT_CODE).originalUrl(ORIGINAL_URL)
                    .expirationDate(LocalDateTime.now().minusDays(1))
                    .clickCount(0).build();

            when(repository.findByShortCode(SHORT_CODE)).thenReturn(Optional.of(expirada));

            assertThrows(UrlExpiredException.class,
                    () -> service.redirectToOriginalUrl(SHORT_CODE));

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Deve adicionar https:// em URL sem protocolo")
        void deveAdicionarProtocoloSeAusente() {
            ShortenedUrl semProtocolo = ShortenedUrl.builder()
                    .shortCode(SHORT_CODE).originalUrl("www.example.com")
                    .expirationDate(LocalDateTime.now().plusDays(1))
                    .clickCount(0).build();

            when(repository.findByShortCode(SHORT_CODE)).thenReturn(Optional.of(semProtocolo));
            when(repository.save(any())).thenReturn(semProtocolo);

            Optional<String> result = service.redirectToOriginalUrl(SHORT_CODE);

            assertTrue(result.isPresent());
            assertTrue(result.get().startsWith("https://"));
        }
    }

    // -------------------------------------------------------------------------
    // deleteShortenedUrl
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("deleteShortenedUrl()")
    class DeleteShortenedUrl {

        @Test
        @DisplayName("Deve deletar URL existente e retornar true")
        void deveDeletarUrlExistente() {
            when(repository.findByShortCode(SHORT_CODE)).thenReturn(Optional.of(entity));
            doNothing().when(repository).delete(entity);

            boolean result = service.deleteShortenedUrl(SHORT_CODE);

            assertTrue(result);
            verify(repository).delete(entity);
        }

        @Test
        @DisplayName("Deve retornar false para short code inexistente")
        void deveRetornarFalseParaCodigoInexistente() {
            when(repository.findByShortCode("naoexiste")).thenReturn(Optional.empty());

            boolean result = service.deleteShortenedUrl("naoexiste");

            assertFalse(result);
            verify(repository, never()).delete(any());
        }
    }

    // -------------------------------------------------------------------------
    // getStats
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("getStats()")
    class GetStats {

        @Test
        @DisplayName("Deve retornar estatísticas quando URL encontrada")
        void deveRetornarEstatisticas() {
            when(repository.findByShortCode(SHORT_CODE)).thenReturn(Optional.of(entity));
            when(mapper.toDetailsResponse(entity)).thenReturn(detailsResponse);

            Optional<DetailsUrlResponse> result = service.getStats(SHORT_CODE);

            assertTrue(result.isPresent());
            assertEquals(0, result.get().clickCount());
            assertEquals(ORIGINAL_URL, result.get().originalUrl());
        }

        @Test
        @DisplayName("Deve lançar UrlNotFoundException quando URL não encontrada")
        void deveLancarExcecaoQuandoNaoEncontrada() {
            when(repository.findByShortCode("naoexiste")).thenReturn(Optional.empty());

            assertThrows(UrlNotFoundException.class,
                    () -> service.getStats("naoexiste"));
        }
    }

    // -------------------------------------------------------------------------
    // listAll
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("listAll()")
    class ListAll {

        @Test
        @DisplayName("Deve listar URLs paginadas corretamente")
        void deveListarUrlsPaginadas() {
            var pageable = PageRequest.of(0, 10);
            var page = new PageImpl<>(List.of(entity), pageable, 1);

            when(repository.findAll(pageable)).thenReturn(page);
            when(mapper.toResponse(entity)).thenReturn(response);

            ShortenUrlListResponse result = service.listAll(pageable);

            assertNotNull(result);
            assertEquals(1, result.shortenUrlList().size());
            assertEquals(1L, result.totalElements());
            assertEquals(0, result.page());
            assertEquals(10, result.size());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há URLs")
        void deveRetornarListaVaziaQuandoSemUrls() {
            var pageable = PageRequest.of(0, 10);
            var emptyPage = new PageImpl<ShortenedUrl>(List.of(), pageable, 0);

            when(repository.findAll(pageable)).thenReturn(emptyPage);

            ShortenUrlListResponse result = service.listAll(pageable);

            assertNotNull(result);
            assertTrue(result.shortenUrlList().isEmpty());
            assertEquals(0L, result.totalElements());
        }
    }
}
