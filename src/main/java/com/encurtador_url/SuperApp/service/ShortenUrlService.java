package com.encurtador_url.SuperApp.service;

import com.encurtador_url.SuperApp.dto.request.ShortenUrlRequest;
import com.encurtador_url.SuperApp.dto.response.ShortenUrlResponse;
import com.encurtador_url.SuperApp.exception.InvalidUrlException;
import com.encurtador_url.SuperApp.exception.UrlExpiredException;

import java.util.Optional;

/**
 * Interface para serviço de gerenciamento de URLs encurtadas
 */
public interface ShortenUrlService {

    /**
     * Encurta uma URL original
     * Se a URL já foi encurtada antes, retorna o código existente
     *
     * @param request contém a URL original, alias opcional e expiração
     * @return DTO com informações da URL encurtada
     * @throws InvalidUrlException se URL for inválida
     */
    ShortenUrlResponse shortenUrl(ShortenUrlRequest request);

    /**
     * Recupera uma URL encurtada pelo seu short code
     *
     * @param shortCode código curto
     * @return DTO com informações da URL, ou Optional vazio
     */
    Optional<ShortenUrlResponse> getShortenedUrl(String shortCode);

    /**
     * Redireciona para a URL original e incrementa o contador de cliques
     * Valida se a URL está expirada
     *
     * @param shortCode código curto
     * @return URL original se encontrada e não expirada
     * @throws UrlExpiredException se URL estiver expirada
     */
    Optional<String> redirectToOriginalUrl(String shortCode);

    /**
     * Deleta uma URL encurtada
     *
     * @param shortCode código curto
     * @return true se deletado, false se não encontrado
     */
    boolean deleteShortenedUrl(String shortCode);

    /**
     * Obtém estatísticas de uma URL encurtada
     *
     * @param shortCode código curto
     * @return DTO com informações, ou Optional vazio
     */
    Optional<ShortenUrlResponse> getStats(String shortCode);
}
