package com.encurtador_url.SuperApp.service;

import com.encurtador_url.SuperApp.dto.request.ShortenUrlRequest;
import com.encurtador_url.SuperApp.dto.response.DetailsUrlResponse;
import com.encurtador_url.SuperApp.dto.response.ShortenUrlListResponse;
import com.encurtador_url.SuperApp.dto.response.ShortenUrlResponse;
import com.encurtador_url.SuperApp.exception.UrlExpiredException;
import com.encurtador_url.SuperApp.exception.UrlInvalidaExceptionException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Interface para serviço de gerenciamento de URLs encurtadas
 */
public interface ShortenUrlService {

    ShortenUrlResponse shortenUrl(ShortenUrlRequest request);

    Optional<ShortenUrlResponse> getShortenedUrl(String shortCode);

    Optional<String> redirectToOriginalUrl(String shortCode);

    boolean deleteShortenedUrl(String shortCode);

    Optional<DetailsUrlResponse> getStats(String shortCode);

    /**
     * Lista todas as URLs encurtadas com paginação
     *
     * @param pageable configuração de paginação e ordenação
     * @return Page contendo ShortenUrlListResponse com a lista de urls
     */
    ShortenUrlListResponse listAll(Pageable pageable);
}
