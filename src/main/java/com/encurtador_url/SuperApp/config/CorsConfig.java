package com.encurtador_url.SuperApp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração global de CORS para permitir requisições de qualquer origem.
 * Isso garante que todos os endpoints — incluindo o redirect (/{shortCode}) —
 * respondam corretamente a requisições cross-origin e preflights OPTIONS.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .exposedHeaders("Location", "Access-Control-Allow-Origin")
                .allowCredentials(false)
                .maxAge(3600);
    }
}

