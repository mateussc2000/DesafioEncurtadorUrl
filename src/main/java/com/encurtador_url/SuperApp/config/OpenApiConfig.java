package com.encurtador_url.SuperApp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do OpenAPI / Swagger UI.
 * Registra o esquema de segurança X-API-Key para que o cadeado apareça
 * nos endpoints protegidos e o header possa ser enviado pelo Swagger UI.
 */
@Configuration
public class OpenApiConfig {

    public static final String API_KEY_SCHEME = "X-API-Key";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("URL Shortener API")
                        .version("1.0")
                        .description("API para encurtar URLs. Operações de escrita exigem o header **X-API-Key**."))
                .components(new Components()
                        .addSecuritySchemes(API_KEY_SCHEME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name(API_KEY_SCHEME)
                                        .description("Chave de API configurada em `app.api-key` (application.properties)")));
    }
}

