package com.encurtador_url.SuperApp.dto.request;

import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO para requisição de encurtamento de URL
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortenUrlRequest {
    /**
     * URL original a ser encurtada (obrigatório)
     */
    @NonNull
    private String originalUrl;

    /**
     * Opcional: alias customizado para a URL encurtada
     */
    private String customAlias;

    /**
     * Opcional: data de expiração da URL encurtada
     */
    private LocalDateTime expirationDate;
}
