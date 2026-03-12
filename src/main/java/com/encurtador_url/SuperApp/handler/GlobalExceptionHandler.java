package com.encurtador_url.SuperApp.handler;

import com.encurtador_url.SuperApp.dto.response.ErrorResponse;
import com.encurtador_url.SuperApp.enums.ErrorCodeEnum;
import com.encurtador_url.SuperApp.exception.MapperException;
import com.encurtador_url.SuperApp.exception.RepositoryException;
import com.encurtador_url.SuperApp.exception.UrlExpiredException;
import com.encurtador_url.SuperApp.exception.UrlInvalidaExceptionException;
import com.encurtador_url.SuperApp.exception.UrlNotFoundException;
import com.encurtador_url.SuperApp.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * Handler global para exceções da API.
 * Toda exceção do projeto deve estender {@link com.encurtador_url.SuperApp.exception.AbstractException}
 * e ter seu código registrado em {@link ErrorCodeEnum}.
 *
 * <p>O {@link ErrorResponse} já carrega {@code codigo} e {@code message} diretamente do enum,
 * portanto não é necessário nenhum campo {@code details} redundante para exceções de domínio.
 * O tipo genérico {@code T} de {@link ErrorResponse} fica reservado para usos futuros onde
 * informações extras estruturadas sejam necessárias (ex: lista de campos inválidos).</p>
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // -------------------------------------------------------------------------
    // Exceções de URL
    // -------------------------------------------------------------------------

    /**
     * Trata {@link UrlInvalidaExceptionException} — {@code 400 BAD_REQUEST}
     * Código: {@link ErrorCodeEnum#ERRO_URL_INVALIDA}
     */
    @ExceptionHandler(UrlInvalidaExceptionException.class)
    public ResponseEntity<ErrorResponse<?>> handleUrlInvalidaException(
            UrlInvalidaExceptionException ex, WebRequest request) {

        log.warn("[{}] URL inválida: {}", ErrorCodeEnum.ERRO_URL_INVALIDA.getCodigo(), ex.getMessage());
        return buildResponse(ErrorCodeEnum.ERRO_URL_INVALIDA, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Trata {@link UrlExpiredException} — {@code 410 GONE}
     * Código: {@link ErrorCodeEnum#ERRO_URL_EXPIRADA}
     */
    @ExceptionHandler(UrlExpiredException.class)
    public ResponseEntity<ErrorResponse<?>> handleUrlExpiredException(
            UrlExpiredException ex, WebRequest request) {

        log.warn("[{}] URL expirada: {}", ErrorCodeEnum.ERRO_URL_EXPIRADA.getCodigo(), ex.getMessage());
        return buildResponse(ErrorCodeEnum.ERRO_URL_EXPIRADA, HttpStatus.GONE, request);
    }

    /**
     * Trata {@link UrlNotFoundException} — {@code 404 NOT_FOUND}
     * Código: {@link ErrorCodeEnum#ERRO_URL_NAO_ENCONTRADA}
     */
    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<ErrorResponse<?>> handleUrlNotFoundException(
            UrlNotFoundException ex, WebRequest request) {

        log.warn("[{}] URL não encontrada: {}", ErrorCodeEnum.ERRO_URL_NAO_ENCONTRADA.getCodigo(), ex.getMessage());
        return buildResponse(ErrorCodeEnum.ERRO_URL_NAO_ENCONTRADA, HttpStatus.NOT_FOUND, request);
    }

    // -------------------------------------------------------------------------
    // Exceções de Validação
    // -------------------------------------------------------------------------

    /**
     * Trata {@link ValidationException} — {@code 400 BAD_REQUEST}
     * Código: {@link ErrorCodeEnum#ERRO_PARAMETRO_INVALIDO}
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse<?>> handleValidationException(
            ValidationException ex, WebRequest request) {

        log.warn("[{}] Parâmetro inválido: {}", ErrorCodeEnum.ERRO_PARAMETRO_INVALIDO.getCodigo(), ex.getMessage());
        return buildResponse(ErrorCodeEnum.ERRO_PARAMETRO_INVALIDO, HttpStatus.BAD_REQUEST, request);
    }

    // -------------------------------------------------------------------------
    // Exceções de Repositório / Banco de Dados
    // -------------------------------------------------------------------------

    /**
     * Trata {@link RepositoryException} — {@code 503 SERVICE_UNAVAILABLE} por padrão,
     * ou o status HTTP específico quando informado na exceção.
     * Código: {@link ErrorCodeEnum#ERRO_CONEXAO_BANCO_DADOS}
     */
    @ExceptionHandler(RepositoryException.class)
    public ResponseEntity<ErrorResponse<?>> handleRepositoryException(
            RepositoryException ex, WebRequest request) {

        log.error("[{}] Erro de repositório: {}", ErrorCodeEnum.ERRO_CONEXAO_BANCO_DADOS.getCodigo(), ex.getMessage(), ex);

        HttpStatus status = ex.hasRepositoryErrorStatusCode()
                ? HttpStatus.resolve(ex.getHttpRepositoryErrorStatusCode())
                : HttpStatus.SERVICE_UNAVAILABLE;

        if (status == null) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
        }

        return buildResponse(ErrorCodeEnum.ERRO_CONEXAO_BANCO_DADOS, status, request);
    }

    // -------------------------------------------------------------------------
    // Exceções de Mapeamento
    // -------------------------------------------------------------------------

    /**
     * Trata {@link MapperException} — {@code 500 INTERNAL_SERVER_ERROR}
     * Código: {@link ErrorCodeEnum#ERRO_MAPEAMENTO}
     */
    @ExceptionHandler(MapperException.class)
    public ResponseEntity<ErrorResponse<?>> handleMapperException(
            MapperException ex, WebRequest request) {

        log.error("[{}] Erro de mapeamento: {}", ErrorCodeEnum.ERRO_MAPEAMENTO.getCodigo(), ex.getMessage(), ex);
        return buildResponse(ErrorCodeEnum.ERRO_MAPEAMENTO, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    // -------------------------------------------------------------------------
    // Fallback genérico
    // -------------------------------------------------------------------------

    /**
     * Trata qualquer exceção não mapeada — {@code 500 INTERNAL_SERVER_ERROR}
     * Código: {@link ErrorCodeEnum#ERRO_SISTEMICO}
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse<?>> handleGlobalException(
            Exception ex, WebRequest request) {

        log.error("[{}] Erro inesperado: {}", ErrorCodeEnum.ERRO_SISTEMICO.getCodigo(), ex.getMessage(), ex);
        return buildResponse(ErrorCodeEnum.ERRO_SISTEMICO, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private ResponseEntity<ErrorResponse<?>> buildResponse(
            ErrorCodeEnum errorCodeEnum, HttpStatus status, WebRequest request) {

        String path = request.getDescription(false).replace("uri=", "");
        return new ResponseEntity<>(new ErrorResponse<>(errorCodeEnum, status.value(), path), status);
    }
}
