package com.encurtador_url.SuperApp.service;

import com.encurtador_url.SuperApp.dto.request.ShortenUrlRequest;
import com.encurtador_url.SuperApp.dto.response.DetailsUrlResponse;
import com.encurtador_url.SuperApp.dto.response.ShortenUrlListResponse;
import com.encurtador_url.SuperApp.dto.response.ShortenUrlResponse;
import com.encurtador_url.SuperApp.exception.*;
import com.encurtador_url.SuperApp.model.ShortenedUrl;
import com.encurtador_url.SuperApp.repository.ShortenedUrlRepository;
import com.encurtador_url.SuperApp.util.ShortCodeGenerator;
import com.encurtador_url.SuperApp.mapper.ShortenUrlMapper;
import com.encurtador_url.SuperApp.validations.UrlBusinessValidator;
import com.encurtador_url.SuperApp.validations.UrlRequestValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Implementação do serviço para gerenciar URLs encurtadas
 * Com tratamento robusto de exceções específicas
 */
@Service
@Slf4j
@Transactional
public class ShortenUrlServiceImpl implements ShortenUrlService {

    @Autowired
    private ShortenedUrlRepository repository;

    @Autowired
    private UrlRequestValidator requestValidator;

    @Autowired
    private UrlBusinessValidator businessValidator;

    @Autowired
    private ShortenUrlMapper mapper;

    @Value("${app.shortener.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final int MAX_RETRIES = 10;

    /**
     * Encurta uma URL original
     * Se a URL já foi encurtada antes, retorna o código existente
     *
     * @param request contém a URL original, alias opcional e expiração
     * @return DTO com informações da URL encurtada
     * @throws UrlInvalidaExceptionException se URL for inválida
     * @throws ValidationException se validações falharem
     * @throws RepositoryException se houver erro ao acessar banco
     * @throws MapperException se houver erro ao mapear
     */
    @Override
    public ShortenUrlResponse shortenUrl(ShortenUrlRequest request) {
        log.debug("Iniciando encurtamento de URL: {}", request.getOriginalUrl());

        try {
            // Validar URL original
            requestValidator.validateOriginalUrl(request.getOriginalUrl());
            log.debug("URL original validada com sucesso");

            // Validar alias customizado se fornecido
            if (request.getCustomAlias() != null && !request.getCustomAlias().isBlank()) {
                requestValidator.validateCustomAlias(request.getCustomAlias());
                log.debug("Alias customizado validado: {}", request.getCustomAlias());
            }

            // Validar data de expiração se fornecida
            if (request.getExpirationDate() != null) {
                requestValidator.validateExpirationDate(request.getExpirationDate());
                log.debug("Data de expiração validada: {}", request.getExpirationDate());
            }

            String originalUrl = request.getOriginalUrl().trim();

            // Verifica se a URL já foi encurtada antes
            String existingCode = businessValidator.checkExistingUrl(originalUrl);
            if (existingCode != null) {
                Optional<ShortenedUrl> existing = repository.findByShortCode(existingCode);
                if (existing.isPresent()) {
                    log.info("URL já existente encontrada: {} -> {}", originalUrl, existingCode);
                    return mapper.toResponse(existing.get());
                }
            }

            // Valida unicidade do alias customizado
            String customAlias = request.getCustomAlias();
            if (customAlias != null && !customAlias.isBlank()) {
                customAlias = customAlias.trim();
                businessValidator.validateCustomAliasUniqueness(customAlias);
                log.debug("Unicidade do alias validada: {}", customAlias);
            } else {
                customAlias = null;
            }

            // Gera short code ou usa customAlias
            String shortCode = customAlias != null ? customAlias : generateUniqueShortCode();

            // Gera a URL encurtada completa usando o basepath configurado
            String shortUrl = baseUrl + "/" + shortCode;

            // Cria e salva a nova URL encurtada
            ShortenedUrl shortenedUrl = ShortenedUrl.builder()
                .shortCode(shortCode)
                .shortUrl(shortUrl)
                .originalUrl(originalUrl)
                .customAlias(customAlias)
                .expirationDate(request.getExpirationDate() == null ? LocalDateTime.now().plusYears(1) : request.getExpirationDate()) // EXPIRA EM 1 ANO POR PADRÃO
                .clickCount(0)
                .build();

            ShortenedUrl saved = repository.save(shortenedUrl);
            log.info("URL encurtada criada com sucesso: {} -> {} (alias: {})",
                     originalUrl, shortCode, customAlias);

            return mapper.toResponse(saved);

        } catch (UrlInvalidaExceptionException | ValidationException | RepositoryException | MapperException ex) {
            log.error("Erro durante encurtamento de URL: {}", ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Erro inesperado durante encurtamento de URL", ex);
            throw new RuntimeException("Erro inesperado ao encurtar URL", ex);
        }
    }

    /**
     * Recupera uma URL encurtada pelo seu short code
     *
     * @param shortCode código curto
     * @return DTO com informações da URL, ou Optional vazio
      * @throws UrlNotFoundException se URL não for encontrada
     * @throws RepositoryException se houver erro ao acessar banco
     * @throws MapperException se houver erro ao mapear
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<ShortenUrlResponse> getShortenedUrl(String shortCode) {
        log.debug("Buscando URL encurtada: {}", shortCode);

        try {
            Optional<ShortenedUrl> url = repository.findByShortCode(shortCode);
            if (url.isEmpty()) {
                log.warn("URL encurtada não encontrada: {}", shortCode);
                throw new UrlNotFoundException();
            }

            log.debug("URL encurtada encontrada: {}", shortCode);
            return url.map(mapper::toResponse);

        } catch (RepositoryException | MapperException | UrlNotFoundException ex) {
            log.error("Erro ao buscar URL encurtada: {}", shortCode, ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Erro inesperado ao buscar URL encurtada", ex);
            throw new RuntimeException("Erro inesperado ao buscar URL encurtada", ex);
        }
    }

    /**
     * Redireciona para a URL original e incrementa o contador de cliques
     * Valida se a URL está expirada
     *
     * @param shortCode código curto
     * @return URL original se encontrada e não expirada
     * @throws UrlExpiredException se URL estiver expirada
     * @throws UrlNotFoundException se URL não for encontrada
     * @throws RepositoryException se houver erro ao acessar banco
     */
    @Override
    public Optional<String> redirectToOriginalUrl(String shortCode) {
        log.debug("Iniciando redirecionamento para: {}", shortCode);

        try {
            Optional<ShortenedUrl> url = repository.findByShortCode(shortCode);
            if (url.isEmpty()) {
                log.warn("URL encurtada não encontrada para redirecionamento: {}", shortCode);
                throw new UrlNotFoundException();
            }

            ShortenedUrl shortenedUrl = url.get();

            // Verifica expiração
            if (shortenedUrl.getExpirationDate() != null &&
                LocalDateTime.now().isAfter(shortenedUrl.getExpirationDate())) {
                log.warn("Tentativa de acesso à URL expirada: {} (expirou em: {})",
                         shortCode, shortenedUrl.getExpirationDate());
                throw new UrlExpiredException();
            }

            // Incrementa clicks e atualiza último acesso
            shortenedUrl.setClickCount(shortenedUrl.getClickCount() + 1);
            shortenedUrl.setLastAccessed(LocalDateTime.now());
            repository.save(shortenedUrl);

            log.info("Redirecionamento bem-sucedido: {} (cliques: {})",
                     shortCode, shortenedUrl.getClickCount());

            String originalUrl = shortenedUrl.getOriginalUrl();
            if (!originalUrl.startsWith("http://") && !originalUrl.startsWith("https://")) {
                originalUrl = "https://" + originalUrl;
            }
            return Optional.of(originalUrl);

        } catch (UrlExpiredException | UrlNotFoundException ex) {
            log.warn("Exceção esperada ao redirecionar: {}", ex.getMessage());
            throw ex;
        } catch (RepositoryException ex) {
            log.error("Erro de banco ao redirecionar: {}", shortCode, ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Erro inesperado ao redirecionar", ex);
            throw new RuntimeException("Erro inesperado ao redirecionar URL", ex);
        }
    }

    /**
     * Deleta uma URL encurtada
     *
     * @param shortCode código curto
     * @return true se deletado, false se não encontrado
     * @throws RepositoryException se houver erro ao acessar banco
     */
    @Override
    public boolean deleteShortenedUrl(String shortCode) {
        log.debug("Iniciando deleção de URL: {}", shortCode);

        try {
            Optional<ShortenedUrl> url = repository.findByShortCode(shortCode);
            if (url.isEmpty()) {
                log.warn("URL não encontrada para deleção: {}", shortCode);
                return false;
            }

            repository.delete(url.get());
            log.info("URL deletada com sucesso: {}", shortCode);
            return true;

        } catch (RepositoryException ex) {
            log.error("Erro durante deleção de URL: {}", shortCode, ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Erro inesperado ao deletar URL", ex);
            throw new RuntimeException("Erro inesperado ao deletar URL", ex);
        }
    }

    /**
     * Obtém estatísticas de uma URL encurtada
     *
     * @param shortCode código curto
     * @return DTO com informações, ou Optional vazio
     * @throws UrlNotFoundException se URL não for encontrada
     * @throws RepositoryException se houver erro ao acessar banco
     * @throws MapperException se houver erro ao mapear
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<DetailsUrlResponse> getStats(String shortCode) {
        log.debug("Buscando estatísticas da URL: {}", shortCode);

        try {
            Optional<ShortenedUrl> url = repository.findByShortCode(shortCode);
            if (url.isEmpty()) {
                log.warn("URL não encontrada para estatísticas: {}", shortCode);
                throw new UrlNotFoundException();
            }

            log.debug("Estatísticas encontradas para: {}", shortCode);
            return url.map(mapper::toDetailsResponse);

        } catch (RepositoryException | MapperException | UrlNotFoundException ex) {
            log.error("Erro ao buscar estatísticas: {}", shortCode, ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Erro inesperado ao buscar estatísticas", ex);
            throw new RuntimeException("Erro inesperado ao buscar estatísticas da URL", ex);
        }
    }

    /**
     * Gera um short code único que ainda não existe no banco
     * Utiliza retry com limite máximo
     *
     * @return short code único
     * @throws RuntimeException se houver erro ao validar unicidade
     */
    private String generateUniqueShortCode() {
        log.debug("Gerando short code único");

        for (int i = 0; i < MAX_RETRIES; i++) {
            String shortCode = ShortCodeGenerator.generateRandomCode();

            if (!repository.existsByShortCode(shortCode)) {
                log.debug("Short code único gerado: {} (tentativa: {})", shortCode, i + 1);
                return shortCode;
            }
        }

        log.error("Não foi possível gerar short code único após {} tentativas", MAX_RETRIES);
        throw new RuntimeException(
            "Não foi possível gerar um short code único após " + MAX_RETRIES + " tentativas"
        );
    }

    /**
     * Lista todas as URLs encurtadas com paginação
     *
     * @param pageable configuração de paginação e ordenação
     * @return ShortenUrlListResponse com a lista paginada e metadados
     */
    @Override
    @Transactional(readOnly = true)
    public ShortenUrlListResponse listAll(Pageable pageable) {
        log.debug("Listando URLs com paginação - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        try {
            Page<ShortenedUrl> page = repository.findAll(pageable);

            var list = page.getContent()
                    .stream()
                    .map(mapper::toResponse)
                    .toList();

            log.info("URLs listadas: {} de {} total", list.size(), page.getTotalElements());

            return new ShortenUrlListResponse(
                    list,
                    page.getNumber(),
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.isLast()
            );

        } catch (Exception ex) {
            log.error("Erro ao listar URLs", ex);
            throw new RuntimeException("Erro ao listar URLs", ex);
        }
    }

}
