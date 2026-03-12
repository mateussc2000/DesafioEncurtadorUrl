package com.encurtador_url.SuperApp.mapper;

import com.encurtador_url.SuperApp.dto.response.DetailsUrlResponse;
import com.encurtador_url.SuperApp.dto.response.ShortenUrlResponse;
import com.encurtador_url.SuperApp.model.ShortenedUrl;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper para converter ShortenedUrl para DTOs de resposta
 * Implementado com MapStruct para melhor performance e geração automática de código
 */
@Mapper
public interface ShortenUrlMapper {

    /**
     * Converte ShortenedUrl para ShortenUrlResponse
     *
     * @param shortenedUrl entidade do banco
     * @return ShortenUrlResponse com campos essenciais
     */
    @Mapping(source = "shortCode", target = "id")
    ShortenUrlResponse toResponse(ShortenedUrl shortenedUrl);

    /**
     * Converte ShortenedUrl para DetailsUrlResponse
     * Inclui informações de cliques
     *
     * @param shortenedUrl entidade do banco
     * @return DetailsUrlResponse com campos essenciais + clickCount
     */
    @Mapping(source = "shortCode", target = "id")
    DetailsUrlResponse toDetailsResponse(ShortenedUrl shortenedUrl);
}

