package com.encurtador_url.SuperApp.service;

import com.encurtador_url.SuperApp.dto.request.ShortenUrlRequest;
import com.encurtador_url.SuperApp.dto.response.ShortenUrlResponse;
import com.encurtador_url.SuperApp.exception.InvalidUrlException;
import com.encurtador_url.SuperApp.exception.UrlExpiredException;
import com.encurtador_url.SuperApp.model.ShortenedUrl;
import com.encurtador_url.SuperApp.repository.ShortenedUrlRepository;
import com.encurtador_url.SuperApp.util.ShortCodeGenerator;
import com.encurtador_url.SuperApp.mapper.ShortenUrlMapper;
import com.encurtador_url.SuperApp.validations.UrlBusinessValidator;
import com.encurtador_url.SuperApp.validations.UrlRequestValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Implementação do serviço para gerenciar URLs encurtadas
 */
@Service
@Slf4j
@RequiredArgsConstructor
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

    private static final int MAX_RETRIES = 10;

    /**
     * Encurta uma URL original
     * Se a URL já foi encurtada antes, retorna o código existente
     *
     * @param request contém a URL original, alias opcional e expiração
     * @return DTO com informações da URL encurtada
     * @throws InvalidUrlException se URL for inválida
     */
    @Override
    public ShortenUrlResponse shortenUrl(ShortenUrlRequest request) {
        /*TODO: Verificar se é suficiente ou retornar todo objeto*/
        log.debug("Processando encurtamento de URL: {}", request.getOriginalUrl());

        // Validações conforme PDF
        requestValidator.validateOriginalUrl(request.getOriginalUrl());
        if (request.getCustomAlias() != null && !request.getCustomAlias().isBlank()) {
            requestValidator.validateCustomAlias(request.getCustomAlias());
        }
        if (request.getExpirationDate() != null) {
            requestValidator.validateExpirationDate(request.getExpirationDate());
        }

        String originalUrl = request.getOriginalUrl().trim();

        // Verifica se a URL já foi encurtada antes
        String existingCode = businessValidator.checkExistingUrl(originalUrl);
        if (existingCode != null) {
            // Retorna a URL existente
            Optional<ShortenedUrl> existing = repository.findByShortCode(existingCode);
            if (existing.isPresent()) {
                return mapper.toResponse(existing.get());
            }
        }

        // Valida unicidade do alias customizado
        String customAlias = request.getCustomAlias();
        if (customAlias != null && !customAlias.isBlank()) {
            customAlias = customAlias.trim();
            businessValidator.validateCustomAliasUniqueness(customAlias);
        } else {
            customAlias = null;
        }

        // Gera short code ou usa customAlias
        String shortCode = customAlias != null ? customAlias : generateUniqueShortCode();

        // Cria e salva a nova URL encurtada
        ShortenedUrl shortenedUrl = ShortenedUrl.builder()
            .shortCode(shortCode)
            .originalUrl(originalUrl)
            .customAlias(customAlias)
            .expirationDate(request.getExpirationDate())
            .clickCount(0)
            .build();

        ShortenedUrl saved = repository.save(shortenedUrl);
        log.info("URL encurtada criada: {} -> {} (alias: {})", originalUrl, shortCode, customAlias);

        return mapper.toResponse(saved);
    }

    /**
     * Recupera uma URL encurtada pelo seu short code
     *
     * @param shortCode código curto
     * @return DTO com informações da URL, ou Optional vazio
     */
    @Transactional(readOnly = true)
    public Optional<ShortenUrlResponse> getShortenedUrl(String shortCode) {
        Optional<ShortenedUrl> url = repository.findByShortCode(shortCode);
        return url.map(mapper::toResponse);
    }

    /**
     * Redireciona para a URL original e incrementa o contador de cliques
     * Valida se a URL está expirada
     *
     * @param shortCode código curto
     * @return URL original se encontrada e não expirada
     * @throws UrlExpiredException se URL estiver expirada
     */
    public Optional<String> redirectToOriginalUrl(String shortCode) {
        Optional<ShortenedUrl> url = repository.findByShortCode(shortCode);

        if (url.isPresent()) {
            ShortenedUrl shortenedUrl = url.get();

            // Verifica expiração
            if (shortenedUrl.getExpirationDate() != null &&
                LocalDateTime.now().isAfter(shortenedUrl.getExpirationDate())) {
                log.warn("Tentativa de acesso à URL expirada: {}", shortCode);
                throw new UrlExpiredException("URL expirada em " + shortenedUrl.getExpirationDate());
            }

            // Incrementa clicks e atualiza último acesso
            shortenedUrl.setClickCount(shortenedUrl.getClickCount() + 1);
            shortenedUrl.setLastAccessed(LocalDateTime.now());
            repository.save(shortenedUrl);

            log.info("Redirecionamento: {} (clicks: {})", shortCode, shortenedUrl.getClickCount());
            return Optional.of(shortenedUrl.getOriginalUrl());
        }

        return Optional.empty();
    }

    /**
     * Deleta uma URL encurtada
     *
     * @param shortCode código curto
     * @return true se deletado, false se não encontrado
     */
    public boolean deleteShortenedUrl(String shortCode) {
        Optional<ShortenedUrl> url = repository.findByShortCode(shortCode);

        if (url.isPresent()) {
            repository.delete(url.get());
            log.info("URL deletada: {}", shortCode);
            return true;
        }

        return false;
    }

    /**
     * Obtém estatísticas de uma URL encurtada
     *
     * @param shortCode código curto
     * @return DTO com informações, ou Optional vazio
     */
    @Transactional(readOnly = true)
    public Optional<ShortenUrlResponse> getStats(String shortCode) {
        Optional<ShortenedUrl> url = repository.findByShortCode(shortCode);
        return url.map(mapper::toResponse);
    }

    /**
     * Gera um short code único que ainda não existe no banco
     * Utiliza retry com limite máximo
     *
     * @return short code único
     */
    private String generateUniqueShortCode() {
        for (int i = 0; i < MAX_RETRIES; i++) {
            String shortCode = ShortCodeGenerator.generateRandomCode();

            if (!repository.existsByShortCode(shortCode)) {
                return shortCode;
            }
        }

        // Se atingir o limite de retries, usa código hash
        throw new RuntimeException("Não foi possível gerar um short code único após " + MAX_RETRIES + " tentativas");
    }

}
