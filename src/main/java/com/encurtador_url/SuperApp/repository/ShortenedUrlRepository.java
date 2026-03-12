package com.encurtador_url.SuperApp.repository;

import com.encurtador_url.SuperApp.model.ShortenedUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para operações de banco de dados em URLs encurtadas
 */
@Repository
public interface ShortenedUrlRepository extends JpaRepository<ShortenedUrl, String> {

    /**
     * Busca uma URL encurtada pelo seu short code
     * @param shortCode código curto da URL
     * @return Optional com a URL encontrada
     */
    Optional<ShortenedUrl> findByShortCode(String shortCode);

    /**
     * Verifica se um short code já existe
     * @param shortCode código curto
     * @return true se existe, false caso contrário
     */
    boolean existsByShortCode(String shortCode);

    /**
     * Busca uma URL encurtada pela URL original
     * @param originalUrl URL original
     * @return Optional com a URL encontrada
     */
    Optional<ShortenedUrl> findByOriginalUrl(String originalUrl);

    /**
     * Busca uma URL encurtada pelo alias customizado
     * @param customAlias alias customizado
     * @return Optional com a URL encontrada
     */
    Optional<ShortenedUrl> findByCustomAlias(String customAlias);

    /**
     * Verifica se um alias customizado já existe
     * @param customAlias alias customizado
     * @return true se existe, false caso contrário
     */
    boolean existsByCustomAlias(String customAlias);
}
