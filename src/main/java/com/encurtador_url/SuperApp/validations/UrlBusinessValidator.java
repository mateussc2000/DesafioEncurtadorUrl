package com.encurtador_url.SuperApp.validations;

import com.encurtador_url.SuperApp.exception.InvalidUrlException;
import com.encurtador_url.SuperApp.repository.ShortenedUrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Validador para regras de negócio que precisam de acesso ao banco
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class UrlBusinessValidator {

    private final ShortenedUrlRepository repository;

    /**
     * Valida se um alias customizado já existe no banco
     * @param customAlias alias a validar
     * @throws InvalidUrlException se alias já existe
     */
    public void validateCustomAliasUniqueness(String customAlias) {
        log.debug("Validando unicidade do alias customizado: {}", customAlias);

        if (customAlias != null && !customAlias.isBlank()) {
            if (repository.existsByCustomAlias(customAlias.trim())) {
                throw new InvalidUrlException("Alias customizado '" + customAlias + "' já existe");
            }
        }

        log.debug("Alias customizado é único");
    }

    /**
     * Valida se uma URL já foi encurtada (retorna o código existente se sim)
     * @param originalUrl URL a verificar
     * @return código existente ou null se não existe
     */
    public String checkExistingUrl(String originalUrl) {
        log.debug("Verificando se URL já existe: {}", originalUrl);

        var existing = repository.findByOriginalUrl(originalUrl.trim());
        if (existing.isPresent()) {
            String existingCode = existing.get().getShortCode();
            log.info("URL já existe com código: {}", existingCode);
            return existingCode;
        }

        log.debug("URL não existe ainda, pode ser encurtada");
        return null;
    }
}
