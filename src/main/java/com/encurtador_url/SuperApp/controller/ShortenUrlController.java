package com.encurtador_url.SuperApp.controller;

import com.encurtador_url.SuperApp.dto.request.ShortenUrlRequest;
import com.encurtador_url.SuperApp.dto.response.ShortenUrlResponse;
import com.encurtador_url.SuperApp.service.ShortenUrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controller REST para gerenciar URLs encurtadas
 */
@RestController
@RequestMapping("")
@Slf4j
@Tag(name = "URL Shortener", description = "API para encurtar e gerenciar URLs")
@CrossOrigin(origins = "*")
public class ShortenUrlController {

    @Autowired
    private ShortenUrlService service;

    /**
     * Cria uma nova URL encurtada
     *
     * @param request contém a URL original
     * @return ResponseEntity com a URL encurtada criada
     */
    @PostMapping("/v1/urls")
    @Operation(summary = "Encurtar uma URL", description = "Cria um identificador curto (ex: abc123) para uma URL longa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "URL encurtada criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Requisição inválida"),
    })
    public ResponseEntity<ShortenUrlResponse> shortenUrl(@RequestBody ShortenUrlRequest request) {
        ShortenUrlResponse response = service.shortenUrl(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtém informações de uma URL encurtada
     *
     * @param shortCode código curto
     * @return ResponseEntity com informações da URL
     */
    @GetMapping("/api/urls/{shortCode}")
    @Operation(summary = "Obter informações da URL", description = "Retorna a URL original e estatísticas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "URL encontrada"),
        @ApiResponse(responseCode = "404", description = "URL não encontrada"),
    })
    public ResponseEntity<ShortenUrlResponse> getUrl(@PathVariable String shortCode) {
        Optional<ShortenUrlResponse> response = service.getShortenedUrl(shortCode);
        return response.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.notFound().build());
    }


    /**
     * Obtém estatísticas de uma URL encurtada
     *
     * @param shortCode código curto
     * @return ResponseEntity com estatísticas
     */
    @GetMapping("/stats/{shortCode}")
    @Operation(summary = "Obter estatísticas", description = "Retorna números de cliques e último acesso")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estatísticas obtidas"),
        @ApiResponse(responseCode = "404", description = "URL não encontrada"),
    })
    public ResponseEntity<ShortenUrlResponse> getStats(@PathVariable String shortCode) {
        Optional<ShortenUrlResponse> response = service.getStats(shortCode);
        return response.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Deleta uma URL encurtada
     *
     * @param shortCode código curto
     * @return ResponseEntity vazio
     */
    @DeleteMapping("/{shortCode}")
    @Operation(summary = "Deletar URL", description = "Remove uma URL encurtada do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "URL deletada com sucesso"),
        @ApiResponse(responseCode = "404", description = "URL não encontrada"),
    })
    public ResponseEntity<Void> deleteUrl(@PathVariable String shortCode) {
        boolean deleted = service.deleteShortenedUrl(shortCode);

        if (deleted) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}
