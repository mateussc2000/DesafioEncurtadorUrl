package com.encurtador_url.SuperApp.controller;

import com.encurtador_url.SuperApp.dto.request.ShortenUrlRequest;
import com.encurtador_url.SuperApp.dto.response.ShortenUrlResponse;
import com.encurtador_url.SuperApp.enums.ErrorCodeEnum;
import com.encurtador_url.SuperApp.repository.ShortenedUrlRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração (TAAC) para {@link ShortenUrlController}.
 *
 * <p>Utilizam o contexto Spring completo com H2 em memória, validando:
 * <ul>
 *   <li>Autenticação via {@code ApiKeyFilter}</li>
 *   <li>Fluxo completo encurtar → consultar → estatísticas → deletar</li>
 *   <li>Respostas de erro padronizadas via {@code GlobalExceptionHandler}</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("ShortenUrlController — Testes de Integração (TAAC)")
class ShortenUrlControllerIntegrationTest {

    private static final String API_KEY       = "test-api-key";
    private static final String HEADER_APIKEY = "X-API-Key";
    private static final String BASE_URL      = "/v1/urls";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ShortenedUrlRepository repository;

    @BeforeEach
    void limparBase() {
        repository.deleteAll();
    }

    // =========================================================================
    // POST /v1/urls/ — Criar URL encurtada
    // =========================================================================

    @Nested
    @DisplayName("POST /v1/urls/ — Criar URL encurtada")
    class CriarUrl {

        @Test
        @DisplayName("Deve criar URL encurtada com sucesso (201)")
        void deveCriarUrlComSucesso() throws Exception {
            ShortenUrlRequest request = new ShortenUrlRequest("https://www.google.com", null, null);

            MvcResult result = mockMvc.perform(post(BASE_URL + "/")
                            .header(HEADER_APIKEY, API_KEY)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.originalUrl").value("https://www.google.com"))
                    .andExpect(jsonPath("$.shortUrl").isNotEmpty())
                    .andReturn();

            ShortenUrlResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ShortenUrlResponse.class);
            assertNotNull(response.id());
            assertEquals(6, response.id().length());
        }

        @Test
        @DisplayName("Deve retornar a mesma URL encurtada para URL duplicada (idempotência)")
        void deveRetornarMesmaUrlParaDuplicada() throws Exception {
            ShortenUrlRequest request = new ShortenUrlRequest("https://www.github.com", null, null);
            String body = objectMapper.writeValueAsString(request);

            // Primeira chamada
            MvcResult first = mockMvc.perform(post(BASE_URL + "/")
                            .header(HEADER_APIKEY, API_KEY)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andReturn();

            // Segunda chamada com a mesma URL
            MvcResult second = mockMvc.perform(post(BASE_URL + "/")
                            .header(HEADER_APIKEY, API_KEY)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andReturn();

            ShortenUrlResponse r1 = objectMapper.readValue(first.getResponse().getContentAsString(), ShortenUrlResponse.class);
            ShortenUrlResponse r2 = objectMapper.readValue(second.getResponse().getContentAsString(), ShortenUrlResponse.class);
            assertEquals(r1.id(), r2.id(), "Mesma URL deve retornar o mesmo short code");
        }

        @Test
        @DisplayName("Deve criar URL com alias customizado")
        void deveCriarUrlComAliasCustomizado() throws Exception {
            ShortenUrlRequest request = new ShortenUrlRequest("https://www.example.com", "meu-alias", null);

            mockMvc.perform(post(BASE_URL + "/")
                            .header(HEADER_APIKEY, API_KEY)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value("meu-alias"));
        }

        @Test
        @DisplayName("Deve retornar 400 para URL sem protocolo HTTP/HTTPS")
        void deveRetornar400ParaUrlInvalida() throws Exception {
            ShortenUrlRequest request = new ShortenUrlRequest("ftp://invalido.com", null, null);

            mockMvc.perform(post(BASE_URL + "/")
                            .header(HEADER_APIKEY, API_KEY)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigo").value(ErrorCodeEnum.ERRO_URL_INVALIDA.getCodigo()))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.path").value("/v1/urls/"));
        }

        @Test
        @DisplayName("Deve retornar 401 sem o header X-API-Key")
        void deveRetornar401SemApiKey() throws Exception {
            ShortenUrlRequest request = new ShortenUrlRequest("https://www.example.com", null, null);

            mockMvc.perform(post(BASE_URL + "/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.codigo").value(ErrorCodeEnum.ERRO_API_KEY_AUSENTE.getCodigo()))
                    .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("Deve retornar 401 com X-API-Key inválida")
        void deveRetornar401ComApiKeyInvalida() throws Exception {
            ShortenUrlRequest request = new ShortenUrlRequest("https://www.example.com", null, null);

            mockMvc.perform(post(BASE_URL + "/")
                            .header(HEADER_APIKEY, "chave-errada")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.codigo").value(ErrorCodeEnum.ERRO_API_KEY_INVALIDA.getCodigo()))
                    .andExpect(jsonPath("$.status").value(401));
        }
    }

    // =========================================================================
    // GET /v1/urls/{shortCode} — Consultar URL
    // =========================================================================

    @Nested
    @DisplayName("GET /v1/urls/{shortCode} — Consultar URL")
    class ConsultarUrl {

        @Test
        @DisplayName("Deve retornar a URL encurtada pelo short code (200)")
        void deveRetornarUrlPeloShortCode() throws Exception {
            // Cria via endpoint
            ShortenUrlRequest request = new ShortenUrlRequest("https://www.consulta.com", null, null);
            MvcResult created = mockMvc.perform(post(BASE_URL + "/")
                            .header(HEADER_APIKEY, API_KEY)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn();

            String shortCode = objectMapper.readValue(
                    created.getResponse().getContentAsString(), ShortenUrlResponse.class).id();

            // Consulta — endpoint GET é público (sem API Key)
            mockMvc.perform(get(BASE_URL + "/{shortCode}", shortCode))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(shortCode))
                    .andExpect(jsonPath("$.originalUrl").value("https://www.consulta.com"));
        }

        @Test
        @DisplayName("Deve retornar 404 para short code inexistente")
        void deveRetornar404ParaShortCodeInexistente() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{shortCode}", "naoexiste"))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // GET /v1/urls/{shortCode}/stats — Estatísticas
    // =========================================================================

    @Nested
    @DisplayName("GET /v1/urls/{shortCode}/stats — Estatísticas")
    class Estatisticas {

        @Test
        @DisplayName("Deve retornar estatísticas com clickCount zerado ao criar (200)")
        void deveRetornarEstatisticasAoCriar() throws Exception {
            ShortenUrlRequest request = new ShortenUrlRequest("https://www.stats.com", null, null);
            MvcResult created = mockMvc.perform(post(BASE_URL + "/")
                            .header(HEADER_APIKEY, API_KEY)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn();

            String shortCode = objectMapper.readValue(
                    created.getResponse().getContentAsString(), ShortenUrlResponse.class).id();

            mockMvc.perform(get(BASE_URL + "/{shortCode}/stats", shortCode))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(shortCode))
                    .andExpect(jsonPath("$.clickCount").value(0))
                    .andExpect(jsonPath("$.originalUrl").value("https://www.stats.com"));
        }
    }

    // =========================================================================
    // DELETE /v1/urls/{shortCode} — Deletar URL
    // =========================================================================

    @Nested
    @DisplayName("DELETE /v1/urls/{shortCode} — Deletar URL")
    class DeletarUrl {

        @Test
        @DisplayName("Deve deletar URL existente e retornar 204")
        void deveDeletarUrlExistente() throws Exception {
            ShortenUrlRequest request = new ShortenUrlRequest("https://www.deletar.com", null, null);
            MvcResult created = mockMvc.perform(post(BASE_URL + "/")
                            .header(HEADER_APIKEY, API_KEY)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn();

            String shortCode = objectMapper.readValue(
                    created.getResponse().getContentAsString(), ShortenUrlResponse.class).id();

            mockMvc.perform(delete(BASE_URL + "/{shortCode}", shortCode)
                            .header(HEADER_APIKEY, API_KEY))
                    .andExpect(status().isNoContent());

            // Confirma que foi removido
            mockMvc.perform(get(BASE_URL + "/{shortCode}", shortCode))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve retornar 401 ao deletar sem API Key")
        void deveRetornar401AoDeletarSemApiKey() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/{shortCode}", "qualquer"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.codigo").value(ErrorCodeEnum.ERRO_API_KEY_AUSENTE.getCodigo()));
        }
    }

    // =========================================================================
    // GET /v1/urls/list — Listagem paginada
    // =========================================================================

    @Nested
    @DisplayName("GET /v1/urls/list — Listagem paginada")
    class ListarUrls {

        @Test
        @DisplayName("Deve retornar 204 quando não há URLs cadastradas")
        void deveRetornarListaVazia() throws Exception {
            mockMvc.perform(get(BASE_URL + "/list"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Deve retornar URLs criadas na listagem")
        void deveRetornarUrlsNaListagem() throws Exception {
            // Cria duas URLs
            for (String url : new String[]{"https://www.a.com", "https://www.b.com"}) {
                mockMvc.perform(post(BASE_URL + "/")
                                .header(HEADER_APIKEY, API_KEY)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new ShortenUrlRequest(url, null, null))))
                        .andExpect(status().isCreated());
            }

            mockMvc.perform(get(BASE_URL + "/list"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.shortenUrlList").isArray())
                    .andExpect(jsonPath("$.totalElements").value(2));
        }

        @Test
        @DisplayName("Deve respeitar parâmetros de paginação")
        void deveRespeitarParametrosDePaginacao() throws Exception {
            // Cria 3 URLs
            for (int i = 1; i <= 3; i++) {
                mockMvc.perform(post(BASE_URL + "/")
                                .header(HEADER_APIKEY, API_KEY)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new ShortenUrlRequest("https://www.page" + i + ".com", null, null))))
                        .andExpect(status().isCreated());
            }

            mockMvc.perform(get(BASE_URL + "/list")
                            .param("page", "0")
                            .param("size", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.shortenUrlList.length()").value(2))
                    .andExpect(jsonPath("$.totalElements").value(3))
                    .andExpect(jsonPath("$.totalPages").value(2))
                    .andExpect(jsonPath("$.last").value(false));
        }
    }
}

