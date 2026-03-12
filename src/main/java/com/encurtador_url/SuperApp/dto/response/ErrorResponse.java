package com.encurtador_url.SuperApp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.encurtador_url.SuperApp.enums.ErrorCodeEnum;
import lombok.Getter;

/**
 * DTO para resposta de erro consistente.
 *
 * @param <T> tipo opcional de detalhes extras (ex: objeto da exception, lista de campos inválidos, etc.)
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse<T> {

    private final int status;
    private final String codigo;
    private final String message;
    private final String path;
    private final Long timestamp;
    private final T details;

    public ErrorResponse(ErrorCodeEnum errorCodeEnum, int status, String path, T details) {
        this.status    = status;
        this.codigo    = errorCodeEnum.getCodigo();
        this.message   = errorCodeEnum.getDescricao();
        this.path      = path;
        this.timestamp = System.currentTimeMillis();
        this.details   = details;
    }

    /** Construtor de conveniência sem detalhes extras. */
    public ErrorResponse(ErrorCodeEnum errorCodeEnum, int status, String path) {
        this(errorCodeEnum, status, path, null);
    }
}
